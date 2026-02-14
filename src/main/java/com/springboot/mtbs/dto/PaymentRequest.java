package com.springboot.mtbs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull Long userId,
        @NotBlank String paymentMethod,
        @NotBlank String paymentToken
) {
}
