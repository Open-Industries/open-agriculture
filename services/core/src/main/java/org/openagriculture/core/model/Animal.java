package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One individually identified animal. Lifecycle: ALIVE -&gt; SOLD or DECEASED. */
public class Animal extends CoreResource {

    @JsonProperty("establishment_id")
    private String establishmentId;

    /** BOVINE, OVINE, PORCINE, POULTRY or OTHER. */
    @JsonProperty("species")
    private String species;

    @JsonProperty("breed")
    private String breed;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("birth_date")
    private String birthDate;

    /** Synthetic ear-tag / individual identifier supplied by the caller. */
    @JsonProperty("tag")
    private String tag;

    public String getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(String establishmentId) {
        this.establishmentId = establishmentId;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
