package org.openagriculture.core.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

/**
 * Shared Jackson configuration for the core service.
 * Snake-case property names match the OpenAPI contract and the upstream
 * envelope (tenant_id, schema_version, correlation_id, ...).
 * Unknown fields are ignored: the upstream DomainResource pattern allows an
 * open metadata object, so forward-compatible clients may send extra fields.
 */
public final class JsonSupport {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonSupport() {
    }
}
