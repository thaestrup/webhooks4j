package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookEvent}.
 */
public class WebhookEventTest {

    @Test
    public void testToString() {
        UUID id = UUID.randomUUID();
        WebhookEvent w = new WebhookEvent(id, "test", Collections.singletonMap("t1", "t2"));
        assertEquals("WebhookEvent{id=" + w.getId() + ", webhook=" + id + ", topic=test, data={t1=t2}}", w.toString());
    }

    @Test
    public void testEquals() {
        UUID publisher = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        WebhookEvent w1 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t1", "t2"));
        WebhookEvent w2 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t3", "t4"));
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    public void testNotEquals() {
        WebhookEvent w1 = new WebhookEvent(UUID.randomUUID(), "test", Collections.singletonMap("t1", "t2"));
        WebhookEvent w2 = new WebhookEvent(UUID.randomUUID(), "test", Collections.singletonMap("t1", "t2"));
        assertNotEquals(w1, w2);
        assertNotEquals(null, w1);
        assertNotEquals(w1, new Object());
    }

}
