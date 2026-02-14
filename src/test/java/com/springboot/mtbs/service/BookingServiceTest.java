package com.springboot.mtbs.service;

import com.springboot.mtbs.dao.*;
import com.springboot.mtbs.dto.*;
import com.springboot.mtbs.entity.*;
import com.springboot.mtbs.entity.enums.PaymentStatus;
import com.springboot.mtbs.entity.enums.ReservationStatus;
import com.springboot.mtbs.entity.enums.ShowSeatStatus;
import com.springboot.mtbs.exception.BadRequestException;
import com.springboot.mtbs.exception.ConflictException;
import com.springboot.mtbs.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ShowRepository showRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationSeatRepository reservationSeatRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                userRepository,
                showRepository,
                showSeatRepository,
                reservationRepository,
                reservationSeatRepository,
                paymentRepository,
                new PaymentService()
        );
    }

    @Test
    void holdSeatsLocksSeatsAndCreatesPendingReservation() {
        User user = userWithId(1L);
        Show show = showWithId(10L, 20L, LocalDateTime.now().plusHours(3));
        ShowSeat seat1 = showSeat(101L, 11L, 20L, ShowSeatStatus.AVAILABLE, null, null, 200.0);
        ShowSeat seat2 = showSeat(102L, 12L, 20L, ShowSeatStatus.AVAILABLE, null, null, 250.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(showSeatRepository.lockSeatsForUpdate(10L, List.of(11L, 12L))).thenReturn(List.of(seat1, seat2));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            ReflectionTestUtils.setField(reservation, "id", 500L);
            return reservation;
        });

        HoldBookingResponse response = bookingService.holdSeats(new HoldBookingRequest(1L, 10L, List.of(11L, 12L)));

        assertEquals(500L, response.reservationId());
        assertEquals("PENDING", response.bookingStatus());
        assertEquals(450.0, response.amount());
        assertEquals(ShowSeatStatus.LOCKED, seat1.getStatus());
        assertEquals(user.getId(), seat1.getLockedByUser().getId());
    }

    @Test
    void holdSeatsThrowsForDuplicateSeatIds() {
        HoldBookingRequest request = new HoldBookingRequest(1L, 10L, List.of(11L, 11L));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookingService.holdSeats(request));

        assertEquals("INVALID_SEAT_SELECTION", exception.getCode());
        verifyNoInteractions(userRepository);
    }

    @Test
    void holdSeatsThrowsWhenSeatAlreadyBooked() {
        User user = userWithId(1L);
        Show show = showWithId(10L, 20L, LocalDateTime.now().plusHours(3));
        ShowSeat bookedSeat = showSeat(101L, 11L, 20L, ShowSeatStatus.BOOKED, null, null, 200.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(showSeatRepository.lockSeatsForUpdate(10L, List.of(11L))).thenReturn(List.of(bookedSeat));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookingService.holdSeats(new HoldBookingRequest(1L, 10L, List.of(11L))));

        assertEquals("SEAT_NOT_AVAILABLE", exception.getCode());
    }

    @Test
    void holdSeatsThrowsWhenShowInPast() {
        User user = userWithId(1L);
        Show show = showWithId(10L, 20L, LocalDateTime.now().minusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.holdSeats(new HoldBookingRequest(1L, 10L, List.of(11L))));

        assertEquals("INVALID_SHOW", exception.getCode());
    }

    @Test
    void holdSeatsThrowsWhenSeatIdsNotFoundForShow() {
        User user = userWithId(1L);
        Show show = showWithId(10L, 20L, LocalDateTime.now().plusHours(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(showSeatRepository.lockSeatsForUpdate(10L, List.of(11L, 12L))).thenReturn(List.of());

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.holdSeats(new HoldBookingRequest(1L, 10L, List.of(11L, 12L))));

        assertEquals("INVALID_SEAT_SELECTION", exception.getCode());
    }

    @Test
    void paySuccessConfirmsReservationAndBooksSeats() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(900L, user, ReservationStatus.PENDING, LocalDateTime.now().plusMinutes(4), 450.0);
        ShowSeat lockedSeat = showSeat(77L, 11L, 20L, ShowSeatStatus.LOCKED, user, LocalDateTime.now().plusMinutes(4), 200.0);
        ReservationSeat reservationSeat = reservationSeat(reservation, lockedSeat);

        when(reservationRepository.findById(900L)).thenReturn(Optional.of(reservation));
        when(reservationSeatRepository.findByReservationId(900L)).thenReturn(List.of(reservationSeat));
        when(showSeatRepository.lockByShowSeatIds(List.of(77L))).thenReturn(List.of(lockedSeat));
        when(paymentRepository.findByReservationId(900L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PaymentResponse response = bookingService.pay(900L, new PaymentRequest(1L, "CARD", "tok_ok"));

        assertEquals("CONFIRMED", response.bookingStatus());
        assertEquals("SUCCESS", response.paymentStatus());
        assertNotNull(response.ticketRef());
        assertEquals(ShowSeatStatus.BOOKED, lockedSeat.getStatus());
        assertNull(lockedSeat.getLockedByUser());
    }

    @Test
    void payFailureMarksReservationFailedAndReleasesSeats() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(901L, user, ReservationStatus.PENDING, LocalDateTime.now().plusMinutes(4), 300.0);
        ShowSeat lockedSeat = showSeat(78L, 13L, 20L, ShowSeatStatus.LOCKED, user, LocalDateTime.now().plusMinutes(4), 300.0);
        ReservationSeat reservationSeat = reservationSeat(reservation, lockedSeat);

        when(reservationRepository.findById(901L)).thenReturn(Optional.of(reservation));
        when(reservationSeatRepository.findByReservationId(901L)).thenReturn(List.of(reservationSeat));
        when(showSeatRepository.lockByShowSeatIds(List.of(78L))).thenReturn(List.of(lockedSeat));
        when(paymentRepository.findByReservationId(901L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        PaymentResponse response = bookingService.pay(901L, new PaymentRequest(1L, "CARD", "tok_fail"));

        assertEquals("FAILED", response.bookingStatus());
        assertEquals("FAILED", response.paymentStatus());
        assertEquals(ShowSeatStatus.AVAILABLE, lockedSeat.getStatus());
        assertNull(lockedSeat.getLockedByUser());
    }

    @Test
    void payThrowsWhenReservationExpired() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(902L, user, ReservationStatus.PENDING, LocalDateTime.now().minusMinutes(1), 300.0);

        when(reservationRepository.findById(902L)).thenReturn(Optional.of(reservation));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookingService.pay(902L, new PaymentRequest(1L, "CARD", "tok_any")));

        assertEquals("RESERVATION_EXPIRED", exception.getCode());
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
    }

    @Test
    void payThrowsWhenReservationDoesNotBelongToUser() {
        User owner = userWithId(1L);
        Reservation reservation = reservationWithId(906L, owner, ReservationStatus.PENDING, LocalDateTime.now().plusMinutes(10), 100.0);
        when(reservationRepository.findById(906L)).thenReturn(Optional.of(reservation));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookingService.pay(906L, new PaymentRequest(99L, "CARD", "tok_ok")));

        assertEquals("RESERVATION_ACCESS_DENIED", exception.getCode());
    }

    @Test
    void payReturnsExistingPaymentForConfirmedReservation() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(907L, user, ReservationStatus.CONFIRMED, LocalDateTime.now().plusMinutes(10), 100.0);
        reservation.setTicketRef("TKT-EXISTING");
        Payment payment = new Payment(reservation, 100.0, "CARD", LocalDateTime.now(), "tok_ok");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(reservationRepository.findById(907L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(907L)).thenReturn(Optional.of(payment));

        PaymentResponse response = bookingService.pay(907L, new PaymentRequest(1L, "CARD", "tok_ok"));

        assertEquals("CONFIRMED", response.bookingStatus());
        assertEquals("SUCCESS", response.paymentStatus());
        assertEquals("TKT-EXISTING", response.ticketRef());
    }

    @Test
    void getBookingHistoryReturnsMappedReservations() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(903L, user, ReservationStatus.CONFIRMED, LocalDateTime.now().plusMinutes(2), 500.0);
        ShowSeat showSeat = showSeat(79L, 14L, 20L, ShowSeatStatus.BOOKED, null, null, 500.0);

        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(reservation));
        when(reservationSeatRepository.findByReservationId(903L)).thenReturn(List.of(reservationSeat(reservation, showSeat)));

        List<BookingResponse> history = bookingService.getBookingHistory(1L);

        assertEquals(1, history.size());
        assertEquals(903L, history.get(0).reservationId());
        assertEquals(14L, history.get(0).seatIds().get(0));
    }

    @Test
    void getBookingThrowsWhenUserDoesNotOwnReservation() {
        User owner = userWithId(1L);
        Reservation reservation = reservationWithId(908L, owner, ReservationStatus.PENDING, LocalDateTime.now().plusMinutes(5), 200.0);
        when(reservationRepository.findById(908L)).thenReturn(Optional.of(reservation));

        ConflictException exception = assertThrows(ConflictException.class, () -> bookingService.getBooking(908L, 2L));

        assertEquals("RESERVATION_ACCESS_DENIED", exception.getCode());
    }

    @Test
    void getBookingThrowsWhenReservationMissing() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookingService.getBooking(999L, 1L));

        assertEquals("RESERVATION_NOT_FOUND", exception.getCode());
    }

    @Test
    void cancelConfirmedReservationReleasesSeatsAndMarksRefunded() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(904L, user, ReservationStatus.CONFIRMED, LocalDateTime.now().plusMinutes(5), 300.0);
        reservation.getShow().setShowTime(LocalDateTime.now().plusHours(2));

        ShowSeat bookedSeat = showSeat(80L, 15L, 20L, ShowSeatStatus.BOOKED, null, null, 300.0);
        ReservationSeat reservationSeat = reservationSeat(reservation, bookedSeat);

        Payment payment = new Payment(reservation, 300.0, "CARD", LocalDateTime.now(), "tok");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(reservationRepository.findById(904L)).thenReturn(Optional.of(reservation));
        when(reservationSeatRepository.findByReservationId(904L)).thenReturn(List.of(reservationSeat));
        when(showSeatRepository.lockByShowSeatIds(List.of(80L))).thenReturn(List.of(bookedSeat));
        when(paymentRepository.findByReservationId(904L)).thenReturn(Optional.of(payment));

        BookingResponse response = bookingService.cancel(904L, 1L);

        assertEquals("CANCELLED", response.status());
        assertEquals(ShowSeatStatus.AVAILABLE, bookedSeat.getStatus());
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    @Test
    void cancelThrowsWhenCutoffPassed() {
        User user = userWithId(1L);
        Reservation reservation = reservationWithId(909L, user, ReservationStatus.CONFIRMED, LocalDateTime.now().plusMinutes(5), 300.0);
        reservation.getShow().setShowTime(LocalDateTime.now().plusMinutes(10));

        when(reservationRepository.findById(909L)).thenReturn(Optional.of(reservation));

        ConflictException exception = assertThrows(ConflictException.class, () -> bookingService.cancel(909L, 1L));

        assertEquals("BOOKING_CANCELLATION_NOT_ALLOWED", exception.getCode());
    }

    @Test
    void expireStaleLocksReleasesSeatsAndExpiresReservations() {
        ShowSeat lockedSeat = showSeat(81L, 16L, 20L, ShowSeatStatus.LOCKED, userWithId(1L), LocalDateTime.now().minusMinutes(1), 200.0);
        Reservation expiredReservation = reservationWithId(905L, userWithId(1L), ReservationStatus.PENDING, LocalDateTime.now().minusMinutes(1), 200.0);

        when(showSeatRepository.findByStatusAndLockExpiresAtBefore(eq(ShowSeatStatus.LOCKED), any(LocalDateTime.class)))
                .thenReturn(List.of(lockedSeat));
        when(reservationRepository.findByStatusAndExpiresAtBefore(eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredReservation));

        bookingService.expireStaleLocks();

        assertEquals(ShowSeatStatus.AVAILABLE, lockedSeat.getStatus());
        assertNull(lockedSeat.getLockedByUser());
        assertEquals(ReservationStatus.EXPIRED, expiredReservation.getStatus());
    }

    private User userWithId(Long id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Show showWithId(Long showId, Long screenId, LocalDateTime showTime) {
        Movie movie = new Movie("Movie", "desc", "Action", 120, "PG", null, "English");
        ReflectionTestUtils.setField(movie, "id", 7L);

        Theatre theatre = new Theatre("Theatre", "Location", 4);
        ReflectionTestUtils.setField(theatre, "id", 99L);

        Screen screen = new Screen("Screen", theatre, 120);
        ReflectionTestUtils.setField(screen, "id", screenId);

        Show show = new Show(movie, screen, showTime, 200.0);
        ReflectionTestUtils.setField(show, "id", showId);
        return show;
    }

    private ShowSeat showSeat(Long showSeatId,
                              Long seatId,
                              Long screenId,
                              ShowSeatStatus status,
                              User lockedBy,
                              LocalDateTime lockExpiresAt,
                              Double price) {
        Show show = showWithId(10L, screenId, LocalDateTime.now().plusHours(2));

        Seat seat = new Seat();
        ReflectionTestUtils.setField(seat, "id", seatId);
        seat.setRow("A");
        seat.setSeatNumber(1);
        seat.setScreen(show.getScreen());

        ShowSeat showSeat = new ShowSeat();
        ReflectionTestUtils.setField(showSeat, "id", showSeatId);
        showSeat.setShow(show);
        showSeat.setSeat(seat);
        showSeat.setStatus(status);
        showSeat.setLockedByUser(lockedBy);
        showSeat.setLockExpiresAt(lockExpiresAt);
        showSeat.setPrice(price);
        return showSeat;
    }

    private Reservation reservationWithId(Long reservationId,
                                          User user,
                                          ReservationStatus status,
                                          LocalDateTime expiresAt,
                                          Double amount) {
        Show show = showWithId(10L, 20L, LocalDateTime.now().plusHours(3));
        Reservation reservation = new Reservation(user, show, LocalDateTime.now(), expiresAt, amount);
        ReflectionTestUtils.setField(reservation, "id", reservationId);
        reservation.setStatus(status);
        return reservation;
    }

    private ReservationSeat reservationSeat(Reservation reservation, ShowSeat showSeat) {
        ReservationSeat reservationSeat = new ReservationSeat();
        reservationSeat.setReservation(reservation);
        reservationSeat.setShowSeat(showSeat);
        return reservationSeat;
    }
}
