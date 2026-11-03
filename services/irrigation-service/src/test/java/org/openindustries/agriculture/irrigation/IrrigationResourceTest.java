package org.openindustries.agriculture.irrigation;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class IrrigationResourceTest {

    @Test
    void consumptionDrawsDown() {
        String id = given().contentType("application/json")
                .body("{\"fieldId\":\"11111111-1111-1111-1111-111111111111\",\"waterUnits\":1000}")
                .when().post("/api/v1/water-budgets")
                .then().statusCode(201).body("waterUnits", is(1000))
                .extract().path("id");

        given().contentType("application/json")
                .body("{\"useType\":\"DRIP\",\"units\":100}")
                .when().post("/api/v1/water-budgets/" + id + "/uses")
                .then().statusCode(200)
                .body("remainingAfter", is(900));
    }

    @Test
    void overdrawIsRejected() {
        String id = given().contentType("application/json")
                .body("{\"fieldId\":\"11111111-1111-1111-1111-111111111111\",\"waterUnits\":1000}")
                .when().post("/api/v1/water-budgets")
                .then().statusCode(201)
                .extract().path("id");

        given().contentType("application/json")
                .body("{\"useType\":\"DRIP\",\"units\":5000}")
                .when().post("/api/v1/water-budgets/" + id + "/uses")
                .then().statusCode(422).body("status", is(422));

        given().when().get("/api/v1/water-budgets/" + id)
                .then().statusCode(200).body("waterUnits", is(1000));
    }
}
