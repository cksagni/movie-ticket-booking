package com.springboot.mtbs.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTheatreRequest(
        @NotBlank String name,
        @NotBlank String location
) {
}
