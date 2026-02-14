package com.springboot.mtbs.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record HoldBookingRequest(
        @NotNull Long userId,
        @NotNull Long showId,
        @NotEmpty @Size(min = 1, max = 10) List<Long> seatIds
) {
}
