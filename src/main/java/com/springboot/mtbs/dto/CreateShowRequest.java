package com.springboot.mtbs.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateShowRequest(
        @NotNull Long movieId,
        @NotNull Long screenId,
        @NotNull @Future LocalDateTime showTime,
        @NotNull Double price
) {
}
