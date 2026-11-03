package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A physical site operated by an organization: a farm, a ranch or a plant. */
public class Establishment extends CoreResource {

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("name")
    private String name;

    /** FARM, RANCH or PLANT. */
    @JsonProperty("kind")
    private String kind;

    @JsonProperty("location")
    private String location;

    @JsonProperty("area_ha")
    private Double areaHa;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(Double areaHa) {
        this.areaHa = areaHa;
    }
}
