package org.openindustries.agriculture.agronomy;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Outbound port; default in-memory adapter, production adapter is JPA on this service's own database. */
@ApplicationScoped
public class AgronomyRuleRepository {
    private final ConcurrentHashMap<UUID, AgronomyRule> store = new ConcurrentHashMap<>();

    public AgronomyRule save(AgronomyRule rule) {
        store.put(rule.id, rule);
        return rule;
    }

    public Optional<AgronomyRule> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<AgronomyRule> findAll() {
        return List.copyOf(store.values());
    }

    /** Active rules for a scope, highest priority (lowest number) first. */
    public List<AgronomyRule> activeFor(String cropType) {
        return store.values().stream()
                .filter(r -> r.cropType.equals(cropType) && "ACTIVE".equals(r.status))
                .sorted(Comparator.comparingInt(r -> r.priority))
                .toList();
    }
}
