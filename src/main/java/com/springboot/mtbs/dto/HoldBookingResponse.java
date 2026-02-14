package com.springboot.mtbs.dto;

import java.time.LocalDateTime;

public record HoldBookingResponse(Long reservationId,
                                  LocalDateTime expiresAt,
                                  Double amount,
                                  String bookingStatus) {
}
