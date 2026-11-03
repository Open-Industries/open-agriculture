package org.openagriculture.core.store;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Local outbox for the reference implementation. In the upstream architecture
 * the domain service writes its local transaction and then publishes
 * CloudEvents through an Outbox; here the outbox is in-memory and every
 * event is also logged, so a deployment without a broker still shows the
 * full event stream. A production deployment would replace this class with
 * a broker-backed outbox behind the same append() API.
 */
public class Outbox {

    private final List<JsonNode> events = new ArrayList<>();

    public synchronized void append(JsonNode event) {
        events.add(event);
        System.out.println("OUTBOX " + event.path("type").asText() + " " + event.path("id").asText());
    }

    public synchronized List<JsonNode> events() {
        return new ArrayList<>(events);
    }

    public synchronized void clear() {
        events.clear();
    }
}
