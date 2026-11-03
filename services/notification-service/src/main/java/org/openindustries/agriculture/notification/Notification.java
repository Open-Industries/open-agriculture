package org.openindustries.agriculture.notification;

import java.time.Instant;
import java.util.UUID;

/** A dispatched notification; channel delivery is an outbound adapter concern. */
public class Notification {
    public UUID id;
    public String channel;
    public String recipient;
    public String template;
    public String status;
    public Instant createdAt;

    public static Notification create(String channel, String recipient, String template) {
        Notification n = new Notification();
        n.id = UUID.randomUUID();
        n.channel = channel;
        n.recipient = recipient;
        n.template = template;
        n.status = "QUEUED";
        n.createdAt = Instant.now();
        return n;
    }
}
