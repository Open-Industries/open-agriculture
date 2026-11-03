package org.openagriculture.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openagriculture.core.model.Animal;
import org.openagriculture.core.model.Batch;
import org.openagriculture.core.model.CoreResource;
import org.openagriculture.core.model.CropSeason;
import org.openagriculture.core.model.Establishment;
import org.openagriculture.core.model.Herd;
import org.openagriculture.core.model.Organization;
import org.openagriculture.core.model.Parcel;
import org.openagriculture.core.store.IdempotencyStore;
import org.openagriculture.core.store.InMemoryStore;
import org.openagriculture.core.store.Outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The core domain service: plain Java, no framework types, so the rules are
 * testable without HTTP. Camel routes are a thin adapter over this class.
 *
 * <p>Invariants enforced here, for every entity type:</p>
 * <ul>
 *   <li>tenant_id comes from the caller (header) and scopes all reads and
 *       writes; a tenant can never see or reference another tenant's data.</li>
 *   <li>X-Correlation-ID is required and must be a UUID (upstream envelope).</li>
 *   <li>schema_version is fixed to "1.0"; anything else is rejected.</li>
 *   <li>status must belong to the entity's lifecycle, or the default applies.</li>
 *   <li>references (organization, establishment, parcel, animals, batch
 *       source) must exist in the same tenant.</li>
 *   <li>a repeated create with the same Idempotency-Key returns the original
 *       resource instead of a duplicate.</li>
 *   <li>every successful create appends one CloudEvent to the outbox.</li>
 * </ul>
 */
public class CoreService {

    /** Static contract of one entity type: class, event, lifecycle, required fields, references. */
    public record EntityDef(
            Class<? extends CoreResource> type,
            String eventType,
            String defaultStatus,
            Set<String> allowedStatuses,
            List<String> requiredFields,
            Map<String, String> references) {
    }

    public static final Map<String, EntityDef> DEFS = Map.of(
            "organizations", new EntityDef(Organization.class, "OrganizationRegistered",
                    "ACTIVE", Set.of("ACTIVE", "SUSPENDED"),
                    List.of("name"), Map.of()),
            "establishments", new EntityDef(Establishment.class, "EstablishmentRegistered",
                    "ACTIVE", Set.of("ACTIVE", "INACTIVE"),
                    List.of("organization_id", "name"),
                    Map.of("organization_id", "organizations")),
            "parcels", new EntityDef(Parcel.class, "ParcelRegistered",
                    "ACTIVE", Set.of("ACTIVE", "INACTIVE"),
                    List.of("establishment_id", "name"),
                    Map.of("establishment_id", "establishments")),
            "crop-seasons", new EntityDef(CropSeason.class, "CropSeasonStarted",
                    "PLANNED", Set.of("PLANNED", "ACTIVE", "HARVESTED", "CLOSED"),
                    List.of("parcel_id", "crop", "season"),
                    Map.of("parcel_id", "parcels")),
            "animals", new EntityDef(Animal.class, "AnimalRegistered",
                    "ALIVE", Set.of("ALIVE", "SOLD", "DECEASED"),
                    List.of("establishment_id", "species"),
                    Map.of("establishment_id", "establishments")),
            "herds", new EntityDef(Herd.class, "HerdFormed",
                    "ACTIVE", Set.of("ACTIVE", "DISSOLVED"),
                    List.of("establishment_id", "name", "species"),
                    Map.of("establishment_id", "establishments")),
            "batches", new EntityDef(Batch.class, "BatchCreated",
                    "CREATED", Set.of("CREATED", "TRANSFORMED", "SHIPPED", "CLOSED"),
                    List.of("source_type", "source_id", "product"),
                    Map.of())
    );

    private final ObjectMapper mapper = JsonSupport.MAPPER;
    private final InMemoryStore store = new InMemoryStore();
    private final IdempotencyStore idempotency = new IdempotencyStore();
    private final Outbox outbox = new Outbox();

    public Outbox outbox() {
        return outbox;
    }

    public CoreResource create(String entityType, String tenantId, String correlationId,
                               String causationId, String purpose, String dataClassification,
                               String idempotencyKey, JsonNode body) {
        EntityDef def = requireDef(entityType);
        requireTenant(tenantId);
        requireCorrelationId(correlationId);
        if (body == null || !body.isObject()) {
            throw new BadRequestException("request body must be a JSON object");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            String existingId = idempotency.lookup(tenantId, entityType, idempotencyKey);
            if (existingId != null) {
                CoreResource original = store.get(tenantId, entityType, existingId);
                if (original != null) {
                    return original;
                }
            }
        }

        CoreResource resource;
        try {
            resource = mapper.treeToValue(body, def.type());
        } catch (Exception e) {
            throw new BadRequestException("request body does not match the " + entityType + " schema: " + e.getMessage());
        }

        String id = body.path("id").asText(null);
        if (id != null && !id.isBlank()) {
            if (store.get(tenantId, entityType, id) != null) {
                throw new BadRequestException(entityType + " with id " + id + " already exists");
            }
        } else {
            id = UUID.randomUUID().toString();
        }

        String bodyTenant = body.path("tenant_id").asText(null);
        if (bodyTenant != null && !bodyTenant.isBlank() && !bodyTenant.equals(tenantId)) {
            throw new BadRequestException("tenant_id in body does not match X-Tenant-ID header");
        }
        String schemaVersion = body.path("schema_version").asText(null);
        if (schemaVersion != null && !schemaVersion.isBlank() && !"1.0".equals(schemaVersion)) {
            throw new BadRequestException("unsupported schema_version: " + schemaVersion);
        }
        String status = body.path("status").asText(null);
        if (status == null || status.isBlank()) {
            status = def.defaultStatus();
        } else if (!def.allowedStatuses().contains(status)) {
            throw new BadRequestException("invalid status for " + entityType + ": " + status);
        }
        for (String field : def.requiredFields()) {
            JsonNode value = body.path(field);
            if (value.isMissingNode() || value.isNull()
                    || (value.isTextual() && value.asText().isBlank())) {
                throw new BadRequestException("missing required field: " + field);
            }
        }
        for (Map.Entry<String, String> ref : def.references().entrySet()) {
            String refId = body.path(ref.getKey()).asText(null);
            if (refId != null && !refId.isBlank()
                    && store.get(tenantId, ref.getValue(), refId) == null) {
                throw new BadRequestException("referenced " + ref.getValue() + " not found: " + refId);
            }
        }
        if ("herds".equals(entityType)) {
            JsonNode animalIds = body.path("animal_ids");
            if (animalIds.isArray()) {
                for (JsonNode animalId : animalIds) {
                    if (store.get(tenantId, "animals", animalId.asText()) == null) {
                        throw new BadRequestException("referenced animal not found: " + animalId.asText());
                    }
                }
            }
        }
        if ("batches".equals(entityType)) {
            String sourceType = body.path("source_type").asText();
            String sourceId = body.path("source_id").asText();
            String target = switch (sourceType) {
                case "PARCEL" -> "parcels";
                case "HERD" -> "herds";
                default -> throw new BadRequestException("invalid source_type for batches: " + sourceType);
            };
            if (store.get(tenantId, target, sourceId) == null) {
                throw new BadRequestException("referenced " + target + " not found: " + sourceId);
            }
        }

        String now = Instant.now().toString();
        resource.setId(id);
        resource.setTenantId(tenantId);
        resource.setSchemaVersion("1.0");
        resource.setStatus(status);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);

        store.put(tenantId, entityType, resource);
        idempotency.record(tenantId, entityType, idempotencyKey, id);
        outbox.append(cloudEvent(def.eventType(), resource, correlationId,
                causationId != null && !causationId.isBlank() ? causationId : correlationId,
                purpose != null && !purpose.isBlank() ? purpose : "core-registration",
                dataClassification != null && !dataClassification.isBlank() ? dataClassification : "synthetic"));
        return resource;
    }

    public CoreResource get(String entityType, String tenantId, String id) {
        requireDef(entityType);
        requireTenant(tenantId);
        CoreResource resource = store.get(tenantId, entityType, id);
        if (resource == null) {
            throw new NotFoundException(entityType + " not found: " + id);
        }
        return resource;
    }

    public List<CoreResource> list(String entityType, String tenantId, String statusFilter) {
        EntityDef def = requireDef(entityType);
        requireTenant(tenantId);
        if (statusFilter != null && !statusFilter.isBlank() && !def.allowedStatuses().contains(statusFilter)) {
            throw new BadRequestException("invalid status filter for " + entityType + ": " + statusFilter);
        }
        List<CoreResource> result = new ArrayList<>();
        for (CoreResource resource : store.list(tenantId, entityType)) {
            if (statusFilter == null || statusFilter.isBlank() || statusFilter.equals(resource.getStatus())) {
                result.add(resource);
            }
        }
        return result;
    }

    /**
     * CloudEvents 1.0 JSON. The upstream envelope (correlation_id,
     * causation_id, schema_version, tenant_id, purpose, data-classification)
     * travels as CloudEvents extension attributes; extension names use only
     * lowercase alphanumerics per the CloudEvents spec, so the snake_case
     * logical names map to tenantid, correlationid, causationid,
     * schemaversion, purpose and dataclassification.
     */
    private JsonNode cloudEvent(String type, CoreResource resource, String correlationId,
                                String causationId, String purpose, String dataClassification) {
        ObjectNode event = mapper.createObjectNode();
        event.put("specversion", "1.0");
        event.put("id", UUID.randomUUID().toString());
        event.put("source", "/open-agriculture/core");
        event.put("type", type);
        event.put("time", Instant.now().toString());
        event.put("datacontenttype", "application/json");
        event.put("subject", resource.getId());
        event.put("tenantid", resource.getTenantId());
        event.put("correlationid", correlationId);
        event.put("causationid", causationId);
        event.put("schemaversion", "1.0");
        event.put("purpose", purpose);
        event.put("dataclassification", dataClassification);
        event.set("data", mapper.valueToTree(resource));
        return event;
    }

    private static EntityDef requireDef(String entityType) {
        EntityDef def = DEFS.get(entityType);
        if (def == null) {
            throw new BadRequestException("unknown entity type: " + entityType);
        }
        return def;
    }

    private static void requireTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new BadRequestException("X-Tenant-ID header is required");
        }
    }

    private static void requireCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new BadRequestException("X-Correlation-ID header is required");
        }
        try {
            UUID.fromString(correlationId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("X-Correlation-ID must be a UUID");
        }
    }
}
