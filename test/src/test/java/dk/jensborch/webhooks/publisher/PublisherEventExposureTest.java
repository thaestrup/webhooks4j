package dk.jensborch.webhooks.publisher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import javax.inject.Inject;

import dk.jensborch.webhooks.*;
import dk.jensborch.webhooks.consumer.WebhookRegistry;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for
 * {@link dk.jensborch.webhooks.consumer.PublisherEventExposur}
 */
@QuarkusTest
public class PublisherEventExposureTest {

    @Inject
    WebhookRegistry registry;

    @Inject
    WebhookPublisher publisher;

    private static final String TEST_TOPIC = PublisherEventExposureTest.class.getName();
    private static Webhook webhook;
    private static WebhookEvent event;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), TEST_TOPIC);
        event = new WebhookEvent(webhook.getId(), TEST_TOPIC, new HashMap<>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        registry.register(webhook);
        publisher.publish(event);
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void testGet() {
        given()
                .spec(spec)
                .when()
                .pathParam("id", event.getId())
                .get("publisher-events/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    public void testListTopics() {
        given()
                .spec(spec)
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", TEST_TOPIC)
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testList() {
        given()
                .spec(spec)
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testListUnknownTopics() {
        given()
                .spec(spec)
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", "t1,t2,t3")
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testListFuture() {
        given()
                .spec(spec)
                .when()
                .queryParam("from", ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME))
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

}
