package com.springboot.mtbs.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCityRequest(
        @NotBlank String name,
        @NotBlank String state,
        @NotBlank String country
) {
}
