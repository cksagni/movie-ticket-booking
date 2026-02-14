package com.springboot.mtbs.controller;

import com.springboot.mtbs.dto.*;
import com.springboot.mtbs.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/hold")
    public HoldBookingResponse holdSeats(@Valid @RequestBody HoldBookingRequest request) {
        return bookingService.holdSeats(request);
    }

    @PostMapping("/{reservationId}/pay")
    public PaymentResponse pay(@PathVariable Long reservationId,
                               @Valid @RequestBody PaymentRequest request) {
        return bookingService.pay(reservationId, request);
    }

    @GetMapping("/{reservationId}")
    public BookingResponse getBooking(@PathVariable Long reservationId,
                                      @RequestParam Long userId) {
        return bookingService.getBooking(reservationId, userId);
    }

    @GetMapping
    public List<BookingResponse> getBookingHistory(@RequestParam Long userId) {
        return bookingService.getBookingHistory(userId);
    }

    @PostMapping("/{reservationId}/cancel")
    public BookingResponse cancel(@PathVariable Long reservationId,
                                  @Valid @RequestBody CancelBookingRequest request) {
        return bookingService.cancel(reservationId, request.userId());
    }
}
