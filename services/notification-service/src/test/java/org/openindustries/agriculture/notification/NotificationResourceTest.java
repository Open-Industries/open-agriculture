package org.openindustries.agriculture.notification;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class NotificationResourceTest {

    @Test
    void dispatchNotification() {
        given().contentType("application/json")
                .body("{\"channel\":\"EMAIL\",\"recipient\":\"user@example.org\",\"template\":\"IRRIGATION_ALERT\"}")
                .when().post("/api/v1/notifications")
                .then().statusCode(202).body("status", is("SENT"));
    }
}
