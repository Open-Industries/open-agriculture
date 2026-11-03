package org.openindustries.agriculture.functions.health;

import io.quarkus.funqy.Funq;

/**
 * Serverless function (Knative / OpenShift Serverless).
 * Scores field risk signals (pests, drought stress, disease) into a transparent 0-100 score with rationale.
 * Transparent, auditable rules: NORMAL (< 35), WARNING (35-69), CRITICAL (>= 70);
 * the rationale is returned with the score for audit (ISO/IEC 27002 A.8.15).
 * Stateless by design (12-Factor VI): all state arrives in the request.
 */
public class Function {

    public record Event(String fieldId, int pestIndex, int droughtStress, int diseaseIndex) {
    }

    public record Score(String fieldId, int score, String decision, String rationale) {
    }

    @Funq
    public Score cropHealthScorer(Event event) {
        int score = 0;
        StringBuilder why = new StringBuilder();
        if (event.pestIndex() >= 70) {
            score += 40;
            why.append("pestIndex >= 70; ");
        }
        if (event.droughtStress() >= 60) {
            score += 30;
            why.append("droughtStress >= 60; ");
        }
        if (event.diseaseIndex() >= 50) {
            score += 30;
            why.append("diseaseIndex >= 50; ");
        }
        String decision = score >= 70 ? "CRITICAL" : score >= 35 ? "WARNING" : "NORMAL";
        return new Score(event.fieldId(), score, decision,
                why.length() == 0 ? "no signals" : why.toString().trim());
    }
}
