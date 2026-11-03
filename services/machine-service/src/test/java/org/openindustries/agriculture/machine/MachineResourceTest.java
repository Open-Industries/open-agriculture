package org.openindustries.agriculture.machine;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MachineResourceTest {

    @Test
    void createAndTransition() {
        String id = given().contentType("application/json")
                .body("{\"name\":\"Tractor T-200\",\"machineType\":\"TRACTOR\",\"powerKw\":150}")
                .when().post("/api/v1/machines")
                .then().statusCode(201)
                .body("status", is("AVAILABLE"))
                .body("machineType", is("TRACTOR"))
                .extract().path("id");

        given().when().post("/api/v1/machines/" + id + "/deploy")
                .then().statusCode(200).body("status", is("DEPLOYED"));
    }
}
