package com.springboot.mtbs.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(Long reservationId,
                              Long userId,
                              Long showId,
                              String status,
                              LocalDateTime expiresAt,
                              Double amount,
                              String ticketRef,
                              List<Long> seatIds) {
}
