package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** A managed group (lot) of animals of one species. Lifecycle: ACTIVE -&gt; DISSOLVED. */
public class Herd extends CoreResource {

    @JsonProperty("establishment_id")
    private String establishmentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("species")
    private String species;

    @JsonProperty("animal_ids")
    private List<String> animalIds;

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

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<String> getAnimalIds() {
        return animalIds;
    }

    public void setAnimalIds(List<String> animalIds) {
        this.animalIds = animalIds;
    }
}
