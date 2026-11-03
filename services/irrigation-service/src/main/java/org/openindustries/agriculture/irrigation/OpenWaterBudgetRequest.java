package org.openindustries.agriculture.irrigation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OpenWaterBudgetRequest(
        @NotNull java.util.UUID fieldId,
        @PositiveOrZero long waterUnits) {
}
