package org.openagriculture.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Contract tests: every synthetic example shipped with the core must satisfy
 * the contract that documents it. Request examples are validated against the
 * OpenAPI component schemas (CoreResource merged with each entity schema);
 * event examples are validated against the CloudEvents JSON Schemas. The
 * OpenAPI/AsyncAPI documents themselves are structurally checked (version,
 * paths, required headers, messages).
 *
 * <p>Files are resolved both from the module directory (mvn test) and from
 * the bundle root, so the tests run either way.</p>
 */
class ContractTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private record Case(String path, String schema, String createSchema, List<String> requiredRequestFields,
                        String requestExample, String eventExample, String eventSchema, String eventType) {
    }

    private static final List<Case> CASES = List.of(
            new Case("organizations", "Organization", "CreateOrganization", List.of("name"),
                    "organization.json",
                    "organization-registered.json", "organization-registered.schema.json", "OrganizationRegistered"),
            new Case("establishments", "Establishment", "CreateEstablishment", List.of("organization_id", "name"),
                    "establishment.json",
                    "establishment-registered.json", "establishment-registered.schema.json", "EstablishmentRegistered"),
            new Case("parcels", "Parcel", "CreateParcel", List.of("establishment_id", "name"),
                    "parcel.json",
                    "parcel-registered.json", "parcel-registered.schema.json", "ParcelRegistered"),
            new Case("crop-seasons", "CropSeason", "CreateCropSeason", List.of("parcel_id", "crop", "season"),
                    "crop-season.json",
                    "crop-season-started.json", "crop-season-started.schema.json", "CropSeasonStarted"),
            new Case("animals", "Animal", "CreateAnimal", List.of("establishment_id", "species"),
                    "animal.json",
                    "animal-registered.json", "animal-registered.schema.json", "AnimalRegistered"),
            new Case("herds", "Herd", "CreateHerd", List.of("establishment_id", "name", "species"),
                    "herd.json",
                    "herd-formed.json", "herd-formed.schema.json", "HerdFormed"),
            new Case("batches", "Batch", "CreateBatch", List.of("source_type", "source_id", "product"),
                    "batch.json",
                    "batch-created.json", "batch-created.schema.json", "BatchCreated")
    );

    private static Path resolve(String... candidates) {
        for (String candidate : candidates) {
            Path path = Path.of(candidate);
            if (Files.exists(path)) {
                return path;
            }
        }
        fail("none of the candidate paths exists: " + String.join(", ", candidates));
        return null;
    }

    private Path openapiPath() {
        return resolve("openapi.yaml", "services/core/openapi.yaml");
    }

    private Path asyncapiPath() {
        return resolve("asyncapi.yaml", "services/core/asyncapi.yaml");
    }

    private Path exampleRequest(String file) {
        return resolve("../../examples/core/requests/" + file, "examples/core/requests/" + file);
    }

    private Path exampleEvent(String file) {
        return resolve("../../examples/core/events/" + file, "examples/core/events/" + file);
    }

    private Path eventSchema(String file) {
        return resolve("../../contracts/cloudevents/" + file, "contracts/cloudevents/" + file);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYaml(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return new Yaml().load(in);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> schemasOf(Map<String, Object> openapi) {
        return (Map<String, Object>) ((Map<String, Object>) openapi.get("components")).get("schemas");
    }

    @Test
    void openapiStructure() throws Exception {
        Map<String, Object> openapi = parseYaml(openapiPath());
        assertEquals("3.1.0", openapi.get("openapi"));
        @SuppressWarnings("unchecked")
        Map<String, Object> paths = (Map<String, Object>) openapi.get("paths");
        Map<String, Object> schemas = schemasOf(openapi);
        assertTrue(schemas.containsKey("CoreResource"));
        @SuppressWarnings("unchecked")
        Map<String, Object> coreRequired = (Map<String, Object>) schemas.get("CoreResource");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) coreRequired.get("required");
        // Upstream DomainResource pattern: these four are always required.
        assertTrue(required.containsAll(List.of("id", "schema_version", "status", "updated_at")));
        assertTrue(required.contains("tenant_id"));

        for (Case c : CASES) {
            assertTrue(schemas.containsKey(c.schema()), "missing schema " + c.schema());
            assertTrue(schemas.containsKey(c.createSchema()), "missing schema " + c.createSchema());
            assertTrue(paths.containsKey("/v1/" + c.path()), "missing path /v1/" + c.path());
            assertTrue(paths.containsKey("/v1/" + c.path() + "/{id}"), "missing path /v1/" + c.path() + "/{id}");
            @SuppressWarnings("unchecked")
            Map<String, Object> collection = (Map<String, Object>) paths.get("/v1/" + c.path());
            @SuppressWarnings("unchecked")
            Map<String, Object> post = (Map<String, Object>) collection.get("post");
            assertNotNull(post, "missing POST /v1/" + c.path());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> params = (List<Map<String, Object>>) post.get("parameters");
            // Parameters are $refs into components/parameters; resolve them.
            @SuppressWarnings("unchecked")
            Map<String, Object> componentsParams =
                    (Map<String, Object>) ((Map<String, Object>) openapi.get("components")).get("parameters");
            boolean hasCorrelation = false;
            boolean hasIdempotency = false;
            for (Map<String, Object> param : params) {
                String ref = (String) param.get("$ref");
                if (ref != null && ref.endsWith("/CorrelationId")) {
                    hasCorrelation = true;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> def = (Map<String, Object>) componentsParams.get("CorrelationId");
                    assertEquals("X-Correlation-ID", def.get("name"));
                    assertEquals(Boolean.TRUE, def.get("required"));
                }
                if (ref != null && ref.endsWith("/IdempotencyKey")) {
                    hasIdempotency = true;
                }
            }
            assertTrue(hasCorrelation, "POST /v1/" + c.path() + " must reference CorrelationId");
            assertTrue(hasIdempotency, "POST /v1/" + c.path() + " must reference IdempotencyKey");
        }
    }

    @Test
    void asyncapiStructure() throws Exception {
        Map<String, Object> asyncapi = parseYaml(asyncapiPath());
        assertEquals("3.0.0", asyncapi.get("asyncapi"));
        @SuppressWarnings("unchecked")
        Map<String, Object> channels = (Map<String, Object>) asyncapi.get("channels");
        assertTrue(channels.containsKey("open-agriculture.domain-events.v1"));
        @SuppressWarnings("unchecked")
        Map<String, Object> channel = (Map<String, Object>) channels.get("open-agriculture.domain-events.v1");
        @SuppressWarnings("unchecked")
        Map<String, Object> messages = (Map<String, Object>) channel.get("messages");
        for (Case c : CASES) {
            assertTrue(messages.containsKey(c.eventType()), "missing message " + c.eventType());
        }
    }

    @Test
    void requestExamplesMatchOpenApiSchemas() throws Exception {
        Map<String, Object> openapi = parseYaml(openapiPath());
        Map<String, Object> schemas = schemasOf(openapi);
        for (Case c : CASES) {
            assertTrue(schemas.containsKey(c.createSchema()), "missing schema " + c.createSchema());
            @SuppressWarnings("unchecked")
            Map<String, Object> create = (Map<String, Object>) schemas.get(c.createSchema());
            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) create.get("required");
            assertTrue(required.containsAll(c.requiredRequestFields()),
                    "Create schema " + c.createSchema() + " must require " + c.requiredRequestFields());
            JsonNode schemaNode = MAPPER.valueToTree(create);
            JsonSchema schema = FACTORY.getSchema(schemaNode);
            JsonNode example = MAPPER.readTree(Files.readString(exampleRequest(c.requestExample())));
            Set<ValidationMessage> errors = schema.validate(example);
            assertTrue(errors.isEmpty(),
                    "request example " + c.requestExample() + " fails schema " + c.createSchema() + ": " + errors);
        }
    }

    @Test
    void eventExamplesMatchCloudEventSchemas() throws Exception {
        for (Case c : CASES) {
            JsonNode schemaNode = MAPPER.readTree(Files.readString(eventSchema(c.eventSchema())));
            JsonSchema schema = FACTORY.getSchema(schemaNode);
            JsonNode example = MAPPER.readTree(Files.readString(exampleEvent(c.eventExample())));
            Set<ValidationMessage> errors = schema.validate(example);
            assertTrue(errors.isEmpty(),
                    "event example " + c.eventExample() + " fails schema " + c.eventSchema() + ": " + errors);
            assertEquals(c.eventType(), example.path("type").asText());
            // The CloudEvents datacontenttype is application/json; the
            // AsyncAPI channel declares the transport content type
            // application/cloudevents+json around the same payload.
            assertEquals("application/json", example.path("datacontenttype").asText());
        }
    }
}
