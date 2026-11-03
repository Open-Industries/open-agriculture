package org.openindustries.agriculture.agronomy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateAgronomyRuleRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "WHEAT|CORN|SOY|COFFEE|VEGETABLES") String cropType,
        @Positive long waterUnitsPerDay,
        @Positive int maxWindKmh,
        @Min(1) @Max(100) int priority) {
}
