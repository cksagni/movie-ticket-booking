package com.springboot.mtbs.dto;

import jakarta.validation.constraints.NotNull;

public record CancelBookingRequest(@NotNull Long userId) {
}
