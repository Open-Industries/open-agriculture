package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One crop grown on one parcel during one season.
 * Lifecycle: PLANNED -&gt; ACTIVE -&gt; HARVESTED -&gt; CLOSED.
 */
public class CropSeason extends CoreResource {

    @JsonProperty("parcel_id")
    private String parcelId;

    @JsonProperty("crop")
    private String crop;

    @JsonProperty("season")
    private String season;

    @JsonProperty("planned_start")
    private String plannedStart;

    @JsonProperty("planned_end")
    private String plannedEnd;

    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(String plannedStart) {
        this.plannedStart = plannedStart;
    }

    public String getPlannedEnd() {
        return plannedEnd;
    }

    public void setPlannedEnd(String plannedEnd) {
        this.plannedEnd = plannedEnd;
    }
}
