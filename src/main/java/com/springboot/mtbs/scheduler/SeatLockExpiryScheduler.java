package com.springboot.mtbs.scheduler;

import com.springboot.mtbs.service.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SeatLockExpiryScheduler {

    private final BookingService bookingService;

    public SeatLockExpiryScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelayString = "${booking.lock.expiry.scan-ms:30000}")
    public void releaseExpiredLocks() {
        bookingService.expireStaleLocks();
    }
}
