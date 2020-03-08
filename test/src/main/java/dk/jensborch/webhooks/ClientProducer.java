package dk.jensborch.webhooks;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import dk.jensborch.webhooks.publisher.Publisher;
import dk.jensborch.webhooks.subscriber.Subscriber;

/**
 * CDI producer for getting a JAX-RS client.
 */
@Dependent
public class ClientProducer {

    @Produces
    @Publisher
    public Client getPublisherClient() {
        return ClientBuilder
                .newClient()
                .register(ObjectMapperProvider.class)
                .register(new BasicAuthClientRequestFilter("publisher", "pubpub"));
    }

    @Produces
    @Subscriber
    public Client getSubscriberClient() {
        return ClientBuilder
                .newClient()
                .register(ObjectMapperProvider.class)
                .register(new BasicAuthClientRequestFilter("subscriber", "concon"));
    }

}
