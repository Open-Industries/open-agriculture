package org.openagriculture.core.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotency-key store for creates. A retried create carrying the same
 * Idempotency-Key, tenant and entity type returns the original resource
 * instead of creating a duplicate. Keys are scoped per tenant: one tenant's
 * key can never replay another tenant's result.
 */
public class IdempotencyStore {

    private final Map<String, String> keyToResourceId = new ConcurrentHashMap<>();

    private static String key(String tenantId, String entityType, String idempotencyKey) {
        return tenantId + "|" + entityType + "|" + idempotencyKey;
    }

    public String lookup(String tenantId, String entityType, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return keyToResourceId.get(key(tenantId, entityType, idempotencyKey));
    }

    public void record(String tenantId, String entityType, String idempotencyKey, String resourceId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        keyToResourceId.putIfAbsent(key(tenantId, entityType, idempotencyKey), resourceId);
    }
}
