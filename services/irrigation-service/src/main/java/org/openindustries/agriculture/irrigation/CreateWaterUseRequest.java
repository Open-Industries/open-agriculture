package org.openindustries.agriculture.irrigation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateWaterUseRequest(
        @NotBlank @Pattern(regexp = "DRIP|SPRINKLER|FLOOD") String useType,
        @Positive long units) {
}
