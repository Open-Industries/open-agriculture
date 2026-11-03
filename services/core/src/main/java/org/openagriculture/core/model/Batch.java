package org.openagriculture.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A traceable lot linking a source (a parcel or a herd) to a product.
 * Lifecycle: CREATED -&gt; TRANSFORMED -&gt; SHIPPED -&gt; CLOSED.
 * This is the traceability anchor of the core: every product lot points
 * back to exactly one agricultural or livestock source.
 */
public class Batch extends CoreResource {

    /** PARCEL or HERD. */
    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("product")
    private String product;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("unit")
    private String unit;

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
