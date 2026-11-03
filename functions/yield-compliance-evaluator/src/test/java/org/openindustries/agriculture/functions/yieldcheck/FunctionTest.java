package org.openindustries.agriculture.functions.yieldcheck;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FunctionTest {

    @Test
    void compliant() {
        given().contentType("application/json")
                .body("{\"fieldId\":\"f-1\",\"measuredYieldGapPct\":3,\"targetYieldGapPct\":5,\"measuredSoilHealthPct\":92.0,\"targetSoilHealthPct\":90.0}")
                .when().post("/yieldComplianceEvaluator")
                .then().statusCode(200).body("compliant", is(true)).body("findings.size()", is(0));
    }

    @Test
    void breachIsReported() {
        given().contentType("application/json")
                .body("{\"fieldId\":\"f-2\",\"measuredYieldGapPct\":12,\"targetYieldGapPct\":5,\"measuredSoilHealthPct\":92.0,\"targetSoilHealthPct\":90.0}")
                .when().post("/yieldComplianceEvaluator")
                .then().statusCode(200).body("compliant", is(false)).body("findings.size()", is(1));
    }
}
