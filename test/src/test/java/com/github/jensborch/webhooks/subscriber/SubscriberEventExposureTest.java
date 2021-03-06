package com.github.jensborch.webhooks.subscriber;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test for
 * {@link com.github.jensborch.webhooks.subscriber.SubscriberEventExposure}
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubscriberEventExposureTest {

    @Inject
    WebhookSubscriptions subscriptions;

    private static final String TEST_TOPIC = SubscriberEventExposureTest.class.getName();
    private static Webhook webhook;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TEST_TOPIC);
    }

    @BeforeEach
    public void setUp() {
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    @Order(1)
    public void testPublishEvent() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent(webhook.getId(), TEST_TOPIC, new HashMap<>()))
                .post("subscriber-events")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    public void testList() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .get("subscriber-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testListUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", "unknown")
                .get("subscriber-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testPublishInvalidEvent() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body("{}")
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.VALIDATION_ERROR.toString()));
    }

    @Test
    public void testPublishEventInvalidPublisher() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent(UUID.randomUUID(), TEST_TOPIC, new HashMap<>()))
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.UNKNOWN_PUBLISHER.toString()));
    }

    @Test
    public void testPublishEventUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent(webhook.getId(), "unknown", new HashMap<>()))
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.UNKNOWN_PUBLISHER.toString()));
    }

}
