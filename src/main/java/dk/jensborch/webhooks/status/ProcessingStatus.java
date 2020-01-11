package dk.jensborch.webhooks.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import dk.jensborch.webhooks.WebhookEvent;
import lombok.Data;
import lombok.Setter;

/**
 *
 */
@Data
public class ProcessingStatus {

    @Setter
    ZonedDateTime end;
    @Setter
    Status status;

    private final UUID id;
    private final WebhookEvent event;
    private final ZonedDateTime start;

    public ProcessingStatus(final WebhookEvent event) {
        this.id = UUID.randomUUID();
        this.event = event;
        this.start = ZonedDateTime.now();
        this.status = Status.STARTED;
    }

    public ProcessingStatus end(final boolean sucess) {
        if (sucess) {
            status = Status.SUCCESS;
        } else {
            status = Status.FAILD;
        }
        end = ZonedDateTime.now();
        return this;
    }

    public boolean eligible() {
        return status == Status.FAILD || status == status.STARTED;
    }

    /**
     *
     */
    public enum Status {
        STARTED, FAILD, SUCCESS;
    }
}
