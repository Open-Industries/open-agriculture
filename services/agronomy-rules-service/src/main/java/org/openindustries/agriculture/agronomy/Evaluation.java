package org.openindustries.agriculture.agronomy;

import java.util.UUID;

/** Decision record: ALLOWED (an active rule fully covers the request), DEGRADED (a rule
 *  exists for the scope but cannot meet the requirement), DENIED (no active rule). */
public record Evaluation(String decision, UUID matchedRuleId, String reason) {
}
