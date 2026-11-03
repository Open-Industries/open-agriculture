package org.openindustries.agriculture.field;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound port (hexagonal architecture). Default adapter is in-memory so the reference
 * implementation runs anywhere; the production adapter is JPA against this service's own
 * database (database-per-service, ADR-0003).
 */
@ApplicationScoped
public class FieldRepository {
    private final ConcurrentHashMap<UUID, Field> store = new ConcurrentHashMap<>();

    public Field save(Field entity) {
        store.put(entity.id, entity);
        return entity;
    }

    public Optional<Field> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Field> findAll() {
        return List.copyOf(store.values());
    }
}
