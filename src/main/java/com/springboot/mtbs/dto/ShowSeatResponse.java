package com.springboot.mtbs.dto;

import java.time.LocalDateTime;

public record ShowSeatResponse(Long showSeatId,
                               Long seatId,
                               String seatRow,
                               Integer seatNumber,
                               String status,
                               Double price,
                               LocalDateTime lockExpiresAt) {
}
