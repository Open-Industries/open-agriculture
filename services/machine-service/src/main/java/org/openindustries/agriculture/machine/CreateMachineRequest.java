package org.openindustries.agriculture.machine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateMachineRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "TRACTOR|HARVESTER|DRONE|IRRIGATION_PUMP") String machineType,
        @Positive int powerKw) {
}
