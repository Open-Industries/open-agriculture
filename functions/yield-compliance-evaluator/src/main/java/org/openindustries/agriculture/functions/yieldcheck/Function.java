package org.openindustries.agriculture.functions.yieldcheck;

import io.quarkus.funqy.Funq;

import java.util.ArrayList;
import java.util.List;

/**
 * Serverless function (Knative / OpenShift Serverless).
 * Compares measured yield gap and soil health against seasonal targets; one finding per breach.
 * Compares measured values with targets and returns a verdict with one finding per
 * breach — machine-checkable assurance evidence.
 */
public class Function {

    public record Kpi(String fieldId, int measuredYieldGapPct, int targetYieldGapPct,
                      double measuredSoilHealthPct, double targetSoilHealthPct) {
    }

    public record Verdict(String fieldId, boolean compliant, List<String> findings) {
    }

    @Funq
    public Verdict yieldComplianceEvaluator(Kpi kpi) {
        List<String> findings = new ArrayList<>();
        if (kpi.measuredYieldGapPct() > kpi.targetYieldGapPct()) {
            findings.add("yieldGapPct " + kpi.measuredYieldGapPct() + " exceeds target " + kpi.targetYieldGapPct());
        }
        if (kpi.measuredSoilHealthPct() < kpi.targetSoilHealthPct()) {
            findings.add("soilHealthPct " + kpi.measuredSoilHealthPct() + " below target " + kpi.targetSoilHealthPct());
        }
        return new Verdict(kpi.fieldId(), findings.isEmpty(), findings);
    }
}
