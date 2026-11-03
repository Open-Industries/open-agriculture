package org.openindustries.agriculture.agronomy;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class AgronomyRuleResourceTest {

    @Test
    void activateThenEvaluateAllowed() {
        String id = given().contentType("application/json")
                .body("{\"name\":\"corn-drip-standard\",\"cropType\":\"CORN\",\"waterUnitsPerDay\":500,\"maxWindKmh\":20,\"priority\":1}")
                .when().post("/api/v1/agronomy-rules")
                .then().statusCode(201).body("status", is("DRAFT"))
                .extract().path("id");

        given().when().post("/api/v1/agronomy-rules/" + id + "/activate")
                .then().statusCode(200).body("status", is("ACTIVE"));

        given().contentType("application/json")
                .body("{\"cropType\":\"CORN\",\"requiredMaxWindKmh\":25,\"requiredWaterUnitsPerDay\":400}")
                .when().post("/api/v1/agronomy-rules/evaluate")
                .then().statusCode(200).body("decision", is("ALLOWED"));
    }

    @Test
    void evaluateWithoutRuleIsDenied() {
        given().contentType("application/json")
                .body("{\"cropType\":\"SOY\",\"requiredMaxWindKmh\":25,\"requiredWaterUnitsPerDay\":400}")
                .when().post("/api/v1/agronomy-rules/evaluate")
                .then().statusCode(200).body("decision", is("DENIED"));
    }
}
