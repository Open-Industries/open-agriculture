package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A delimited piece of agricultural land inside an establishment. */
public class Parcel extends CoreResource {

    @JsonProperty("establishment_id")
    private String establishmentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("area_ha")
    private Double areaHa;

    @JsonProperty("soil_type")
    private String soilType;

    public String getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(String establishmentId) {
        this.establishmentId = establishmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAreaHa() {
        return areaHa;
    }

    public void setAreaHa(Double areaHa) {
        this.areaHa = areaHa;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }
}
