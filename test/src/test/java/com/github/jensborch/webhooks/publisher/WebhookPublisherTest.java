package com.github.jensborch.webhooks.publisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.subscriber.TestEventListener;
import com.github.jensborch.webhooks.subscriber.WebhookSubscriptions;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link WebhookPublisher}.
 */
@QuarkusTest
public class WebhookPublisherTest {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    TestEventListener listener;

    @Inject
    WebhookPublisher publisher;

    @Test
    public void testRegister() throws Exception {
        Webhook webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TestEventListener.TOPIC);
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        Map<String, Object> data = new HashMap<>();
        WebhookEvent event = new WebhookEvent(webhook.getId(), TestEventListener.TOPIC, data);
        publisher.publish(event);
        assertThat(listener.getCount(), greaterThan(0));
        assertThat(listener.getEvents().keySet(), hasItems(event.getId()));
    }

}
