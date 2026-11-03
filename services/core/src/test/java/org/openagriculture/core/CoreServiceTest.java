package org.openagriculture.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.openagriculture.core.model.CoreResource;
import org.openagriculture.core.service.BadRequestException;
import org.openagriculture.core.service.CoreService;
import org.openagriculture.core.service.JsonSupport;
import org.openagriculture.core.service.NotFoundException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Service-layer tests: no HTTP, no Camel, pure domain rules. */
class CoreServiceTest {

    private static final ObjectMapper MAPPER = JsonSupport.MAPPER;
    private static final String TENANT = "tenant-demo";
    private static final String CORR = UUID.randomUUID().toString();

    private CoreService service() {
        return new CoreService();
    }

    private ObjectNode org(String name) {
        return MAPPER.createObjectNode().put("name", name);
    }

    @Test
    void createOrganizationHappyPath() {
        CoreService service = service();
        CoreResource created = service.create("organizations", TENANT, CORR, null, null, null, null, org("Finca Ejemplo"));
        assertNotNull(created.getId());
        assertEquals(TENANT, created.getTenantId());
        assertEquals("1.0", created.getSchemaVersion());
        assertEquals("ACTIVE", created.getStatus());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        assertEquals(1, service.outbox().events().size());
        assertEquals("OrganizationRegistered", service.outbox().events().get(0).path("type").asText());
    }

    @Test
    void createRejectsMissingOrInvalidCorrelationId() {
        CoreService service = service();
        assertThrows(BadRequestException.class, () ->
                service.create("organizations", TENANT, null, null, null, null, null, org("X")));
        assertThrows(BadRequestException.class, () ->
                service.create("organizations", TENANT, "  ", null, null, null, null, org("X")));
        assertThrows(BadRequestException.class, () ->
                service.create("organizations", TENANT, "not-a-uuid", null, null, null, null, org("X")));
    }

    @Test
    void createRejectsMissingTenant() {
        assertThrows(BadRequestException.class, () ->
                service().create("organizations", null, CORR, null, null, null, null, org("X")));
    }

    @Test
    void createRejectsUnknownEntityAndStatus() {
        CoreService service = service();
        assertThrows(BadRequestException.class, () ->
                service.create("nope", TENANT, CORR, null, null, null, null, org("X")));
        ObjectNode bad = org("X").put("status", "FLYING");
        assertThrows(BadRequestException.class, () ->
                service.create("organizations", TENANT, CORR, null, null, null, null, bad));
    }

    @Test
    void createRejectsMissingRequiredFieldAndBadReference() {
        CoreService service = service();
        assertThrows(BadRequestException.class, () ->
                service.create("organizations", TENANT, CORR, null, null, null, null,
                        MAPPER.createObjectNode()));
        ObjectNode parcel = MAPPER.createObjectNode()
                .put("establishment_id", "missing-est")
                .put("name", "Lote 1");
        assertThrows(BadRequestException.class, () ->
                service.create("parcels", TENANT, CORR, null, null, null, null, parcel));
    }

    @Test
    void fullChainAndReferenceRules() {
        CoreService service = service();
        CoreResource org = service.create("organizations", TENANT, CORR, null, null, null, null, org("Finca Ejemplo"));
        ObjectNode est = MAPPER.createObjectNode()
                .put("organization_id", org.getId())
                .put("name", "Establecimiento Ejemplo")
                .put("kind", "FARM");
        CoreResource establishment = service.create("establishments", TENANT, CORR, null, null, null, null, est);
        ObjectNode parcel = MAPPER.createObjectNode()
                .put("establishment_id", establishment.getId())
                .put("name", "Lote Norte")
                .put("area_ha", 12.5);
        CoreResource created = service.create("parcels", TENANT, CORR, null, null, null, null, parcel);
        assertEquals("ParcelRegistered", service.outbox().events().get(2).path("type").asText());

        // Batch with an invalid source type is rejected; with a valid parcel it works.
        ObjectNode badBatch = MAPPER.createObjectNode()
                .put("source_type", "FIELD")
                .put("source_id", created.getId())
                .put("product", "maíz grano");
        assertThrows(BadRequestException.class, () ->
                service.create("batches", TENANT, CORR, null, null, null, null, badBatch));
        ObjectNode batch = MAPPER.createObjectNode()
                .put("source_type", "PARCEL")
                .put("source_id", created.getId())
                .put("product", "maíz grano")
                .put("quantity", 800.0)
                .put("unit", "kg");
        CoreResource batchCreated = service.create("batches", TENANT, CORR, null, null, null, null, batch);
        assertEquals("CREATED", batchCreated.getStatus());
    }

    @Test
    void herdAnimalReferencesMustExist() {
        CoreService service = service();
        CoreResource org = service.create("organizations", TENANT, CORR, null, null, null, null, org("Finca Ejemplo"));
        ObjectNode est = MAPPER.createObjectNode()
                .put("organization_id", org.getId())
                .put("name", "Rancho Ejemplo")
                .put("kind", "RANCH");
        CoreResource establishment = service.create("establishments", TENANT, CORR, null, null, null, null, est);
        ObjectNode animal = MAPPER.createObjectNode()
                .put("establishment_id", establishment.getId())
                .put("species", "BOVINE")
                .put("tag", "TAG-001");
        CoreResource createdAnimal = service.create("animals", TENANT, CORR, null, null, null, null, animal);

        ObjectNode badHerd = MAPPER.createObjectNode()
                .put("establishment_id", establishment.getId())
                .put("name", "Rodeo 1")
                .put("species", "BOVINE")
                .set("animal_ids", MAPPER.createArrayNode().add("ghost"));
        assertThrows(BadRequestException.class, () ->
                service.create("herds", TENANT, CORR, null, null, null, null, badHerd));

        ObjectNode herd = MAPPER.createObjectNode()
                .put("establishment_id", establishment.getId())
                .put("name", "Rodeo 1")
                .put("species", "BOVINE")
                .set("animal_ids", MAPPER.createArrayNode().add(createdAnimal.getId()));
        service.create("herds", TENANT, CORR, null, null, null, null, herd);
        assertEquals("HerdFormed",
                service.outbox().events().get(service.outbox().events().size() - 1).path("type").asText());
    }

    @Test
    void duplicateIdempotencyKeyReturnsOriginal() {
        CoreService service = service();
        CoreResource first = service.create("organizations", TENANT, CORR, null, null, null,
                "key-123", org("Primera"));
        CoreResource second = service.create("organizations", TENANT, CORR, null, null, null,
                "key-123", org("Segunda"));
        assertEquals(first.getId(), second.getId());
        assertEquals(1, service.list("organizations", TENANT, null).size());
        CoreResource third = service.create("organizations", TENANT, CORR, null, null, null,
                "key-456", org("Tercera"));
        assertEquals(2, service.list("organizations", TENANT, null).size());
        assertTrue(!third.getId().equals(first.getId()));
    }

    @Test
    void tenantIsolation() {
        CoreService service = service();
        CoreResource created = service.create("organizations", "tenant-a", CORR, null, null, null, null, org("A"));
        assertThrows(NotFoundException.class, () ->
                service.get("organizations", "tenant-b", created.getId()));
        assertTrue(service.list("organizations", "tenant-b", null).isEmpty());
        assertEquals(1, service.list("organizations", "tenant-a", null).size());
    }

    @Test
    void listStatusFilter() {
        CoreService service = service();
        CoreResource org = service.create("organizations", TENANT, CORR, null, null, null, null, org("Finca Ejemplo"));
        ObjectNode est = MAPPER.createObjectNode()
                .put("organization_id", org.getId())
                .put("name", "E1")
                .put("kind", "FARM");
        CoreResource establishment = service.create("establishments", TENANT, CORR, null, null, null, null, est);
        service.create("parcels", TENANT, CORR, null, null, null, null,
                MAPPER.createObjectNode()
                        .put("establishment_id", establishment.getId())
                        .put("name", "Lote Activo"));
        service.create("parcels", TENANT, CORR, null, null, null, null,
                MAPPER.createObjectNode()
                        .put("establishment_id", establishment.getId())
                        .put("name", "Lote Inactivo")
                        .put("status", "INACTIVE"));
        List<CoreResource> active = service.list("parcels", TENANT, "ACTIVE");
        assertEquals(1, active.size());
        assertEquals(2, service.list("parcels", TENANT, null).size());
    }

    @Test
    void outboxEventCarriesEnvelope() {
        CoreService service = service();
        String corr = UUID.randomUUID().toString();
        CoreResource created = service.create("organizations", TENANT, corr, "cause-1",
                "core-registration", "synthetic", null, org("Finca Ejemplo"));
        var event = service.outbox().events().get(0);
        assertEquals("1.0", event.path("specversion").asText());
        assertEquals("/open-agriculture/core", event.path("source").asText());
        assertEquals("OrganizationRegistered", event.path("type").asText());
        assertEquals(TENANT, event.path("tenantid").asText());
        assertEquals(corr, event.path("correlationid").asText());
        assertEquals("cause-1", event.path("causationid").asText());
        assertEquals("1.0", event.path("schemaversion").asText());
        assertEquals("core-registration", event.path("purpose").asText());
        assertEquals("synthetic", event.path("dataclassification").asText());
        assertEquals(created.getId(), event.path("data").path("id").asText());
    }
}
