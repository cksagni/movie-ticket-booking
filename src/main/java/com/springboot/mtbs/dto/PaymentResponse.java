package com.springboot.mtbs.dto;

public record PaymentResponse(String bookingStatus,
                              String paymentStatus,
                              String ticketRef) {
}
