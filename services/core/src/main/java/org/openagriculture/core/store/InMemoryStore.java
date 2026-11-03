package org.openagriculture.core.store;

import org.openagriculture.core.model.CoreResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory state for the reference implementation.
 * Keyed by tenant first: a tenant can only ever see its own entities, which
 * is what makes tenant isolation a property of the store, not of the routes.
 * Deliberately not a database: the upstream scaffold is provider-neutral and
 * this service must run anywhere, including scale-to-zero Knative, without
 * provisioned infrastructure.
 */
public class InMemoryStore {

    private final Map<String, Map<String, Map<String, CoreResource>>> byTenantAndType =
            new ConcurrentHashMap<>();

    private Map<String, CoreResource> bucket(String tenantId, String entityType) {
        return byTenantAndType
                .computeIfAbsent(tenantId, t -> new ConcurrentHashMap<>())
                .computeIfAbsent(entityType, t -> new LinkedHashMap<>());
    }

    public void put(String tenantId, String entityType, CoreResource resource) {
        Map<String, CoreResource> bucket = bucket(tenantId, entityType);
        synchronized (bucket) {
            bucket.put(resource.getId(), resource);
        }
    }

    public CoreResource get(String tenantId, String entityType, String id) {
        Map<String, CoreResource> bucket = bucket(tenantId, entityType);
        synchronized (bucket) {
            return bucket.get(id);
        }
    }

    public List<CoreResource> list(String tenantId, String entityType) {
        Map<String, CoreResource> bucket = bucket(tenantId, entityType);
        synchronized (bucket) {
            return new ArrayList<>(bucket.values());
        }
    }
}
