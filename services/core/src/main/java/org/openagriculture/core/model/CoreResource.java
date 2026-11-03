package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base shape of every core resource, mirroring the upstream DomainResource
 * pattern: required id, schema_version, status and updated_at, plus the
 * multi-tenant envelope field tenant_id. Serialized as snake_case so the
 * JSON matches the OpenAPI contract exactly.
 *
 * <p>schema_version is fixed to "1.0" for this first contract version.
 * Breaking changes require a new schema version and a documented
 * coexistence period (upstream semantic-governance rule).</p>
 */
public abstract class CoreResource {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("schema_version")
    private String schemaVersion = "1.0";

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    protected CoreResource() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
