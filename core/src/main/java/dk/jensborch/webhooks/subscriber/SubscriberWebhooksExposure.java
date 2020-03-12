package dk.jensborch.webhooks.subscriber;

import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEventTopics;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.publisher.PublisherWebhookExposure;
import dk.jensborch.webhooks.validation.ValidUUID;

/**
 * Exposure for registration of webhooks.
 */
@Path("/consumer-webhooks")
@DeclareRoles("subscriber")
@RolesAllowed({"subscriber"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SubscriberWebhooksExposure {

    @Inject
    WebhookSubscriptions subscriper;

    @Inject
    WebhookEventConsumer consumer;

    @POST
    @RolesAllowed({"subscriber"})
    public Response create(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        subscriper.subscribe(webhook);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(PublisherWebhookExposure.class)
                .path(PublisherWebhookExposure.class, "get")
                .build(webhook.getId()))
                .build();
    }

    @PUT
    @RolesAllowed({"subscriber"})
    public Response update(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        Webhook w = findAndMerge(webhook);
        switch (w.getStatus()) {
            case SYNCHRONIZE:
                consumer.sync(w);
                break;
            case UNSUBSCRIBE:
                subscriper.unsubscribe(w.getId());
                break;
            default:
                WebhookError error = new WebhookError(WebhookError.Code.ILLEGAL_STATUS, "Illegal status " + w.getStatus());
                throw new WebhookException(error);
        }
        return Response.ok(webhook).build();
    }

    private Webhook findAndMerge(final Webhook webhook) {
        return subscriper.find(webhook.getId())
                .orElseThrow(() -> throwNotFound(webhook.getId().toString()))
                .state(webhook.getStatus())
                .topics(webhook.getTopics())
                .updated(webhook.getUpdated());
    }

    @GET
    public Response list(@QueryParam("topics") final String topics) {
        return Response.ok(subscriper.list(WebhookEventTopics.parse(topics).getTopics())).build();
    }

    @GET
    @Path("{id}")
    public Response get(@ValidUUID @NotNull @PathParam("id") final String id) {
        return subscriper.find(UUID.fromString(id))
                .map(Response::ok)
                .orElseThrow(() -> throwNotFound(id))
                .build();
    }

    private WebhookException throwNotFound(final String id) {
        WebhookError error = new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found");
        return new WebhookException(error);
    }

}