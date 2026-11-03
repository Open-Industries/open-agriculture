package org.openindustries.agriculture.functions.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FunctionTest {

    @Test
    void healthyIsNormal() {
        given().contentType("application/json")
                .body("{\"fieldId\":\"f-1\",\"pestIndex\":5,\"droughtStress\":10,\"diseaseIndex\":4}")
                .when().post("/cropHealthScorer")
                .then().statusCode(200).body("decision", is("NORMAL")).body("score", is(0));
    }

    @Test
    void stackedSignalsAreCritical() {
        given().contentType("application/json")
                .body("{\"fieldId\":\"f-2\",\"pestIndex\":80,\"droughtStress\":75,\"diseaseIndex\":60}")
                .when().post("/cropHealthScorer")
                .then().statusCode(200).body("decision", is("CRITICAL")).body("score", is(100));
    }
}
