package com.github.jensborch.webhooks.subscriber;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseHandler;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage webhook subscriptions.
 */
@ApplicationScoped
public class WebhookSubscriptions {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookSubscriptions.class);

    @Inject
    @Subscriber
    Client client;

    @Inject
    @Subscriber
    WebhookRepository repo;

    /**
     * Register a webhook to receive events from a publisher. Will throw a
     * {@link WebhookError} runtime exception if registration fails.
     *
     * @param webhook to register.
     */
    public void subscribe(@NotNull @Valid final Webhook webhook) {
        if (repo.find(webhook.getId()).filter(w -> w.getState() != Webhook.State.FAILED).isPresent()) {
            LOG.info("Webhook {} already exists", webhook);
        } else if (webhook.getState() == Webhook.State.SUBSCRIBE) {
            repo.save(webhook.state(Webhook.State.SUBSCRIBING));
            WebhookResponseHandler
                    .type(Response.class)
                    .invocation(client
                            .target(webhook.gePublisherEndpoints().getWebhooks())
                            .request(MediaType.APPLICATION_JSON)
                            .buildPost(Entity.json(webhook.state(Webhook.State.SUBSCRIBE)))
                    )
                    .success(r -> repo.save(webhook.state(Webhook.State.ACTIVE)))
                    .error(e -> {
                        repo.save(webhook.state(Webhook.State.FAILED));
                        throwWebhookException("Failed to register, got error response: " + e);
                    })
                    .exception(e -> {
                        repo.save(webhook.state(Webhook.State.FAILED));
                        throwWebhookException("Failed to register, error processing response", e);
                    })
                    .invoke();
        } else {
            throwWebhookException("Webhook " + webhook.getId() + " status is not " + Webhook.State.SUBSCRIBE);
        }
    }

    /**
     * Unregister a webhook from a publisher. This will throw a
     * {@link WebhookError} runtime exception if it isn't possible to
     * unregister.
     *
     * @param id of webhook to unregister.
     */
    public void unsubscribe(@NotNull final UUID id) {
        Webhook w = find(id).orElseThrow(() -> new WebhookException(
                new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook with id " + id + " not found")));
        unsubscribe(w);
    }

    public void unsubscribe(@NotNull @Valid final Webhook webhook) {
        repo.save(webhook.state(Webhook.State.UNSUBSCRIBING));
        WebhookResponseHandler
                .type(Response.class)
                .invocation(client
                        .target(webhook.gePublisherEndpoints().getWebhooks())
                        .path("{id}")
                        .resolveTemplate("id", webhook.getId())
                        .request()
                        .buildDelete())
                .success(r -> repo.save(webhook.state(Webhook.State.INACTIVE)))
                .error(error -> {
                    if (error.getStatus() == Response.Status.NOT_FOUND.getStatusCode() && error.getCode() == WebhookError.Code.NOT_FOUND) {
                        LOG.info("Webhook {} not found in publisher", error);
                        repo.save(webhook.state(Webhook.State.INACTIVE));
                    } else {
                        repo.save(webhook.state(Webhook.State.FAILED));
                        throwWebhookException("Failed to unregister, got error response: " + error);
                    }
                })
                .exception(e -> {
                    repo.save(webhook.state(Webhook.State.FAILED));
                    throwWebhookException("Failed to unregister, error processing response", e);
                })
                .invoke();
    }

    private void throwWebhookException(final String msg) {
        LOG.error(msg);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, msg)
        );
    }

    private void throwWebhookException(final String msg, final Exception e) {
        LOG.error(msg, e);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, msg), e
        );
    }

    public Optional<Webhook> find(final UUID id) {
        return repo.find(id);
    }

    public Set<Webhook> list(final String... topic) {
        return repo.list(topic);
    }

    public void touch(final UUID id) {
        repo.touch(id);
    }

}
