package org.openindustries.agriculture.agronomy;

import java.util.UUID;

/**
 * AgronomyRule aggregate — a decision rule of this core: for one cropType, it guarantees a
 * capacity (waterUnitsPerDay) and a threshold (maxWindKmh), with a precedence priority.
 * Only ACTIVE rules decide; every decision names its rule and its reason (audit).
 */
public class AgronomyRule {
    public UUID id;
    public String name;
    public String cropType;
    public long waterUnitsPerDay;
    public int maxWindKmh;
    public int priority;
    public String status;

    public static AgronomyRule create(String name, String cropType, long waterUnitsPerDay, int maxWindKmh, int priority) {
        AgronomyRule r = new AgronomyRule();
        r.id = UUID.randomUUID();
        r.name = name;
        r.cropType = cropType;
        r.waterUnitsPerDay = waterUnitsPerDay;
        r.maxWindKmh = maxWindKmh;
        r.priority = priority;
        r.status = "DRAFT";
        return r;
    }
}
