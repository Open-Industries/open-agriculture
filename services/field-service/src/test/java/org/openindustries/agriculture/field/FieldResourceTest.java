package org.openindustries.agriculture.field;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FieldResourceTest {

    @Test
    void createAndFetch() {
        String id = given().contentType("application/json")
                .body("{\"name\":\"North Plot 1\",\"cropType\":\"CORN\",\"hectares\":25,\"regionCode\":\"MX-JAL-01\"}")
                .when().post("/api/v1/fields")
                .then().statusCode(201)
                .body("status", is("ACTIVE"))
                .body("cropType", is("CORN"))
                .extract().path("id");

        given().when().get("/api/v1/fields/" + id)
                .then().statusCode(200).body("status", is("ACTIVE"));
    }

    @Test
    void transitionWithoutBody() {
        // Lifecycle transitions take no body and must not require a Content-Type (no 415).
        String id = given().contentType("application/json")
                .body("{\"name\":\"North Plot 1\",\"cropType\":\"CORN\",\"hectares\":25,\"regionCode\":\"MX-JAL-01\"}")
                .when().post("/api/v1/fields")
                .then().statusCode(201)
                .extract().path("id");

        given().when().post("/api/v1/fields/" + id + "/fallow")
                .then().statusCode(200).body("status", is("FALLOW"));
    }
}
