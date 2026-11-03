package org.openindustries.agriculture.irrigation;

import java.time.Instant;
import java.util.UUID;

/** One guarded consumption record — the auditable evidence of a draw on a WaterBudget. */
public class WaterUse {
    public UUID id;
    public UUID accountId;
    public String useType;
    public long units;
    public long remainingAfter;
    public Instant recordedAt;

    public static WaterUse of(UUID accountId, String useType, long units, long remainingAfter) {
        WaterUse r = new WaterUse();
        r.id = UUID.randomUUID();
        r.accountId = accountId;
        r.useType = useType;
        r.units = units;
        r.remainingAfter = remainingAfter;
        r.recordedAt = Instant.now();
        return r;
    }
}
