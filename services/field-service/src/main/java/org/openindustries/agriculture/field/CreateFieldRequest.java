package org.openindustries.agriculture.field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateFieldRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "WHEAT|CORN|SOY|COFFEE|VEGETABLES") String cropType,
        @Positive int hectares,
        @NotBlank String regionCode) {
}
