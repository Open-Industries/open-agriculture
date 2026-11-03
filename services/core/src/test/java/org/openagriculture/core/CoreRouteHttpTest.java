package org.openagriculture.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.openagriculture.core.service.JsonSupport;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP integration tests, Quarkus style: the Quarkus test extension boots
 * the real application (Camel routes on the Quarkus HTTP server) and
 * RestAssured exercises the routes end to end (headers, status codes,
 * tenant isolation, idempotency, health probe). Same cases as the
 * pre-Quarkus suite; the application instance is shared across the
 * methods of this class, so each case uses its own tenant or key.
 */
@QuarkusTest
class CoreRouteHttpTest {

    private static final ObjectMapper MAPPER = JsonSupport.MAPPER;

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    private static Response post(String path, String tenant, String correlationId,
                                 String idempotencyKey, String body) {
        var req = given()
                .contentType(ContentType.JSON)
                .body(body == null ? "{}" : body);
        if (tenant != null) {
            req.header("X-Tenant-ID", tenant);
        }
        if (correlationId != null) {
            req.header("X-Correlation-ID", correlationId);
        }
        if (idempotencyKey != null) {
            req.header("Idempotency-Key", idempotencyKey);
        }
        return req.when().post(path);
    }

    private static Response get(String path, String tenant, String correlationId) {
        var req = given();
        if (tenant != null) {
            req.header("X-Tenant-ID", tenant);
        }
        if (correlationId != null) {
            req.header("X-Correlation-ID", correlationId);
        }
        return req.when().get(path);
    }

    @Test
    void healthProbeAnswersUp() {
        given().when().get("/health")
                .then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    void missingCorrelationIdRejected() {
        Response r = post("/v1/organizations", "tenant-demo", null, null, "{\"name\":\"X\"}");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().asString().contains("X-Correlation-ID"));
    }

    @Test
    void invalidCorrelationIdRejected() {
        Response r = post("/v1/organizations", "tenant-demo", "not-a-uuid", null, "{\"name\":\"X\"}");
        assertEquals(400, r.statusCode());
    }

    @Test
    void missingTenantRejected() {
        Response r = post("/v1/organizations", null, uuid(), null, "{\"name\":\"X\"}");
        assertEquals(400, r.statusCode());
    }

    @Test
    void happyPathCreateAndGet() throws Exception {
        Response created = post("/v1/organizations", "tenant-demo", uuid(), null,
                "{\"name\":\"Finca Ejemplo\"}");
        assertEquals(201, created.statusCode());
        JsonNode node = MAPPER.readTree(created.body().asString());
        String id = node.path("id").asText();
        assertTrue(!id.isBlank());
        assertEquals("1.0", node.path("schema_version").asText());
        assertEquals("ACTIVE", node.path("status").asText());

        Response fetched = get("/v1/organizations/" + id, "tenant-demo", uuid());
        assertEquals(200, fetched.statusCode());
        assertEquals(id, MAPPER.readTree(fetched.body().asString()).path("id").asText());
    }

    @Test
    void duplicateIdempotencyKeyReturnsOriginal() throws Exception {
        Response first = post("/v1/organizations", "tenant-idem", uuid(), "idem-1",
                "{\"name\":\"Primera\"}");
        Response second = post("/v1/organizations", "tenant-idem", uuid(), "idem-1",
                "{\"name\":\"Segunda\"}");
        assertEquals(201, first.statusCode());
        assertEquals(201, second.statusCode());
        assertEquals(
                MAPPER.readTree(first.body().asString()).path("id").asText(),
                MAPPER.readTree(second.body().asString()).path("id").asText());
        Response list = get("/v1/organizations", "tenant-idem", uuid());
        assertEquals(1, MAPPER.readTree(list.body().asString()).size());
    }

    @Test
    void tenantIsolationOverHttp() throws Exception {
        Response created = post("/v1/organizations", "tenant-a", uuid(), null,
                "{\"name\":\"Solo A\"}");
        String id = MAPPER.readTree(created.body().asString()).path("id").asText();
        Response cross = get("/v1/organizations/" + id, "tenant-b", uuid());
        assertEquals(404, cross.statusCode());
        Response listB = get("/v1/organizations", "tenant-b", uuid());
        assertEquals(0, MAPPER.readTree(listB.body().asString()).size());
    }

    @Test
    void unknownEntityRejected() {
        Response r = post("/v1/nope", "tenant-demo", uuid(), null, "{}");
        assertEquals(400, r.statusCode());
    }
}
