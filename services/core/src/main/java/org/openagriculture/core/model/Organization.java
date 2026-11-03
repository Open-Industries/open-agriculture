package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A company operating in agriculture and/or livestock. An organization IS a tenant. */
public class Organization extends CoreResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("country")
    private String country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
