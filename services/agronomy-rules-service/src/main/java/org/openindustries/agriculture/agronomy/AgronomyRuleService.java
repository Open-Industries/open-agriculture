package org.openindustries.agriculture.agronomy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AgronomyRuleService {

    @Inject
    AgronomyRuleRepository repository;

    public AgronomyRule create(CreateAgronomyRuleRequest request) {
        return repository.save(AgronomyRule.create(request.name(), request.cropType(),
                request.waterUnitsPerDay(), request.maxWindKmh(), request.priority()));
    }

    public AgronomyRule get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException(404, "AgronomyRule not found: " + id));
    }

    public List<AgronomyRule> list() {
        return repository.findAll();
    }

    public AgronomyRule activate(UUID id) {
        AgronomyRule rule = get(id);
        rule.status = "ACTIVE";
        return repository.save(rule);
    }

    public Evaluation evaluate(EvaluateRequest request) {
        var candidates = repository.activeFor(request.cropType());
        if (candidates.isEmpty()) {
            return new Evaluation("DENIED", null,
                    "No active rule for " + request.cropType());
        }
        for (AgronomyRule rule : candidates) {
            if (rule.maxWindKmh <= request.requiredMaxWindKmh()
                    && rule.waterUnitsPerDay >= request.requiredWaterUnitsPerDay()) {
                return new Evaluation("ALLOWED", rule.id,
                        "Rule '" + rule.name + "' covers the request");
            }
        }
        return new Evaluation("DEGRADED", candidates.get(0).id,
                "Active rules exist for " + request.cropType() + " but none meets the requirement");
    }
}
