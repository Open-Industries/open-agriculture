package org.openindustries.agriculture.machine;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Outbound port; default in-memory adapter, production adapter is JPA on this service's own database. */
@ApplicationScoped
public class MachineRepository {
    private final ConcurrentHashMap<UUID, Machine> store = new ConcurrentHashMap<>();

    public Machine save(Machine entity) {
        store.put(entity.id, entity);
        return entity;
    }

    public Optional<Machine> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Machine> findAll() {
        return List.copyOf(store.values());
    }
}
