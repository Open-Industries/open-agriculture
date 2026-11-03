package org.openindustries.agriculture.irrigation;

import java.util.UUID;

/**
 * WaterBudget aggregate — the guarded resource account of this core: a balance/capacity that
 * consumption operations draw from. Core invariant (ADR-0004): it NEVER goes negative;
 * an operation that would overdraw it is rejected and records nothing.
 */
public class WaterBudget {
    public UUID id;
        public java.util.UUID fieldId;
    public long waterUnits;
    public String status;

    public static WaterBudget open(java.util.UUID fieldId, long waterUnits) {
        WaterBudget a = new WaterBudget();
        a.id = UUID.randomUUID();
        a.fieldId = fieldId;
        a.waterUnits = waterUnits;
        a.status = "ACTIVE";
        return a;
    }
}
