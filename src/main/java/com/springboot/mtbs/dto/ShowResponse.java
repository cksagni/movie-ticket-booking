package com.springboot.mtbs.dto;

import java.time.LocalDateTime;

public record ShowResponse(Long id,
                           Long movieId,
                           String movieTitle,
                           LocalDateTime showTime,
                           Double price,
                           Long screenId,
                           String theatreName) {
}
