package org.openindustries.agriculture.agronomy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record EvaluateRequest(
        @NotBlank @Pattern(regexp = "WHEAT|CORN|SOY|COFFEE|VEGETABLES") String cropType,
        @Positive int requiredMaxWindKmh,
        @Positive long requiredWaterUnitsPerDay) {
}
