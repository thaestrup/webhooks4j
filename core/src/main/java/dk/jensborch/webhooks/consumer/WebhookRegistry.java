package dk.jensborch.webhooks.consumer;

import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ApplicationScoped
public class WebhookRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookRegistry.class);

    @Inject
    @Consumer
    Client client;

    @Inject
    @Consumer
    WebhookRepository repo;

    public void registre(final Webhook webhook) {
        //TODO: add status info to webhook
        repo.save(webhook);
        try {
            Response response = client.target(webhook.getPublisher())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(webhook));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                WebhookError error = response.readEntity(WebhookError.class);
                throwWebhookException("Faild to register, got HTTP status code " + response.getStatus() + " " + error);
            }
        } catch (ProcessingException e) {
            throwWebhookException("Faild to register, error prossing response", e);
        }
    }

    private void throwWebhookException(String msg) {
        LOG.error(msg);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTRE_ERROR, msg)
        );
    }

    private void throwWebhookException(String msg, Exception e) {
        LOG.error(msg, e);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTRE_ERROR, msg), e
        );
    }

    public Webhook get(final UUID id) {
        return repo.get(id);
    }

    public Set<Webhook> find(String topic) {
        return repo.find(topic);
    }

}
