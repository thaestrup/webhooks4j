package dk.jensborch.webhooks.publisher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;

import dk.jensborch.webhooks.Webhook;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test for {@link PublisherWebhookExposure}.
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class PublisherWebhookExposureTest {

    private static final String TEST_TOPIC = PublisherWebhookExposureTest.class.getName();
    private RequestSpecification spec;

    @BeforeEach
    public void setUp() {
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    @Order(1)
    public void testCreateWebhook() throws Exception {
        String location = given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .body(new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), TEST_TOPIC)
                        .status(Webhook.Status.REGISTER))
                .post("publisher-webhooks")
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .get(location)
                .then()
                .statusCode(200)
                .body("topics[0]", is(TEST_TOPIC));
    }

    @Test
    @Order(2)
    public void testListWebhooksWithTopics() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("topics", TEST_TOPIC + ",testtest")
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    @Order(2)
    public void testListWebhooksUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("topics", "unknown")
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @Order(2)
    public void testListWebhooks() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

}
