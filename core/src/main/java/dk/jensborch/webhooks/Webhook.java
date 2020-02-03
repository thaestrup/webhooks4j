package dk.jensborch.webhooks;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

/**
 * This class defines a Webhook with a publisher and subscribe URI.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Webhook {

    @Setter
    Boolean active;

    @NotNull
    private final UUID id;
    @NotNull
    private final URI publisher;
    @NotNull
    private final URI consumer;
    @NotNull
    @Size(min = 1)
    private final Set<String> topics;

    public Webhook(final URI publisher, final URI consumer, final Set<String> topics) {
        this.active = true;
        this.id = UUID.randomUUID();
        this.consumer = consumer;
        this.publisher = publisher;
        this.topics = new HashSet<>(topics);
    }

    public Webhook(final URI publisher, final URI consumer, final String... topics) {
        this(publisher, consumer, Arrays.stream(topics).collect(Collectors.toSet()));
    }

}
