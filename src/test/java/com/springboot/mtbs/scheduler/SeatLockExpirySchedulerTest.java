package com.springboot.mtbs.scheduler;

import com.springboot.mtbs.service.BookingService;
import com.springboot.mtbs.service.PaymentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeatLockExpirySchedulerTest {

    @Test
    void releaseExpiredLocksDelegatesToBookingService() {
        class FakeBookingService extends BookingService {
            private boolean called;

            FakeBookingService() {
                super(null, null, null, null, null, null, new PaymentService());
            }

            @Override
            public void expireStaleLocks() {
                called = true;
            }
        }

        FakeBookingService bookingService = new FakeBookingService();
        SeatLockExpiryScheduler scheduler = new SeatLockExpiryScheduler(bookingService);

        scheduler.releaseExpiredLocks();

        assertTrue(bookingService.called);
    }
}
