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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 5;
    private static final int CANCELLATION_CUTOFF_MINUTES = 30;

    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public BookingService(UserRepository userRepository,
                          ShowRepository showRepository,
                          ShowSeatRepository showSeatRepository,
                          ReservationRepository reservationRepository,
                          ReservationSeatRepository reservationSeatRepository,
                          PaymentRepository paymentRepository,
                          PaymentService paymentService) {
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public HoldBookingResponse holdSeats(HoldBookingRequest request) {
        Set<Long> distinctSeatIds = new HashSet<>(request.seatIds());
        if (distinctSeatIds.size() != request.seatIds().size()) {
            throw new BadRequestException("INVALID_SEAT_SELECTION", "Seat IDs in request must be unique");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found for id=" + request.userId()));

        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new NotFoundException("SHOW_NOT_FOUND", "Show not found for id=" + request.showId()));

        if (!show.getShowTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("INVALID_SHOW", "Booking is allowed only for future shows");
        }

        List<ShowSeat> showSeats = showSeatRepository.lockSeatsForUpdate(request.showId(), request.seatIds());
        if (showSeats.size() != request.seatIds().size()) {
            throw new BadRequestException("INVALID_SEAT_SELECTION", "Some seat IDs are invalid for this show");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);
        double totalAmount = 0.0;

        for (ShowSeat showSeat : showSeats) {
            if (!showSeat.getSeat().getScreen().getId().equals(show.getScreen().getId())) {
                throw new BadRequestException("INVALID_SEAT_SELECTION", "Seat does not belong to show's screen");
            }

            boolean lockExpired = showSeat.getStatus() == ShowSeatStatus.LOCKED
                    && showSeat.getLockExpiresAt() != null
                    && showSeat.getLockExpiresAt().isBefore(now);

            if (showSeat.getStatus() == ShowSeatStatus.BOOKED ||
                    (showSeat.getStatus() == ShowSeatStatus.LOCKED && !lockExpired)) {
                throw new ConflictException("SEAT_NOT_AVAILABLE", "One or more seats are not available");
            }

            showSeat.setStatus(ShowSeatStatus.LOCKED);
            showSeat.setLockedByUser(user);
            showSeat.setLockExpiresAt(expiresAt);
            totalAmount += showSeat.getPrice() == null ? show.getPrice() : showSeat.getPrice();
        }

        showSeatRepository.saveAll(showSeats);

        Reservation reservation = new Reservation(user, show, now, expiresAt, totalAmount);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation = reservationRepository.save(reservation);
        Reservation savedReservation = reservation;

        List<ReservationSeat> reservationSeats = showSeats.stream().map(showSeat -> {
            ReservationSeat reservationSeat = new ReservationSeat();
            reservationSeat.setReservation(savedReservation);
            reservationSeat.setShowSeat(showSeat);
            return reservationSeat;
        }).toList();
        reservationSeatRepository.saveAll(reservationSeats);

        return new HoldBookingResponse(
                reservation.getId(),
                reservation.getExpiresAt(),
                reservation.getTotalAmount(),
                reservation.getStatus().name()
        );
    }

    @Transactional
    public PaymentResponse pay(Long reservationId, PaymentRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("RESERVATION_NOT_FOUND", "Reservation not found for id=" + reservationId));

        if (!reservation.getUser().getId().equals(request.userId())) {
            throw new ConflictException("RESERVATION_ACCESS_DENIED", "Reservation does not belong to user");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            Payment existingPayment = paymentRepository.findByReservationId(reservation.getId())
                    .orElseThrow(() -> new NotFoundException("PAYMENT_NOT_FOUND", "Payment record not found"));
            return new PaymentResponse(reservation.getStatus().name(), existingPayment.getStatus().name(), reservation.getTicketRef());
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ConflictException("INVALID_RESERVATION_STATE", "Payment allowed only for pending reservations");
        }

        if (reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            throw new ConflictException("RESERVATION_EXPIRED", "Reservation has expired");
        }

        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationId(reservationId);
        List<Long> showSeatIds = reservationSeats.stream().map(rs -> rs.getShowSeat().getId()).toList();
        List<ShowSeat> seatsToConfirm = showSeatRepository.lockByShowSeatIds(showSeatIds);

        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat showSeat : seatsToConfirm) {
            if (showSeat.getStatus() != ShowSeatStatus.LOCKED
                    || showSeat.getLockedByUser() == null
                    || !showSeat.getLockedByUser().getId().equals(request.userId())
                    || showSeat.getLockExpiresAt() == null
                    || showSeat.getLockExpiresAt().isBefore(now)) {
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
                throw new ConflictException("RESERVATION_EXPIRED", "Seat lock is no longer valid");
            }
        }

        Payment payment = paymentRepository.findByReservationId(reservation.getId()).orElseGet(() -> {
            Payment newPayment = new Payment(
                    reservation,
                    reservation.getTotalAmount(),
                    request.paymentMethod(),
                    LocalDateTime.now(),
                    request.paymentToken()
            );
            newPayment.setStatus(PaymentStatus.INITIATED);
            return paymentRepository.save(newPayment);
        });

        boolean success = paymentService.isPaymentSuccessful(request.paymentToken());
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setTicketRef("TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            for (ShowSeat showSeat : seatsToConfirm) {
                showSeat.setStatus(ShowSeatStatus.BOOKED);
                showSeat.setLockedByUser(null);
                showSeat.setLockExpiresAt(null);
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            reservation.setStatus(ReservationStatus.FAILED);
            for (ShowSeat showSeat : seatsToConfirm) {
                showSeat.setStatus(ShowSeatStatus.AVAILABLE);
                showSeat.setLockedByUser(null);
                showSeat.setLockExpiresAt(null);
            }
        }

        paymentRepository.save(payment);
        reservationRepository.save(reservation);
        showSeatRepository.saveAll(seatsToConfirm);

        return new PaymentResponse(
                reservation.getStatus().name(),
                payment.getStatus().name(),
                reservation.getTicketRef()
        );
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("RESERVATION_NOT_FOUND", "Reservation not found for id=" + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ConflictException("RESERVATION_ACCESS_DENIED", "Reservation does not belong to user");
        }

        List<Long> seatIds = reservationSeatRepository.findByReservationId(reservationId)
                .stream()
                .map(reservationSeat -> reservationSeat.getShowSeat().getSeat().getId())
                .toList();

        return new BookingResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getShow().getId(),
                reservation.getStatus().name(),
                reservation.getExpiresAt(),
                reservation.getTotalAmount(),
                reservation.getTicketRef(),
                seatIds
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingHistory(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(reservation -> {
                    List<Long> seatIds = reservationSeatRepository.findByReservationId(reservation.getId())
                            .stream()
                            .map(reservationSeat -> reservationSeat.getShowSeat().getSeat().getId())
                            .toList();
                    return new BookingResponse(
                            reservation.getId(),
                            reservation.getUser().getId(),
                            reservation.getShow().getId(),
                            reservation.getStatus().name(),
                            reservation.getExpiresAt(),
                            reservation.getTotalAmount(),
                            reservation.getTicketRef(),
                            seatIds
                    );
                })
                .toList();
    }

    @Transactional
    public BookingResponse cancel(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("RESERVATION_NOT_FOUND", "Reservation not found for id=" + reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ConflictException("RESERVATION_ACCESS_DENIED", "Reservation does not belong to user");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ConflictException("BOOKING_CANCELLATION_NOT_ALLOWED", "Only pending or confirmed reservations can be cancelled");
        }

        LocalDateTime cutoff = reservation.getShow().getShowTime().minusMinutes(CANCELLATION_CUTOFF_MINUTES);
        if (LocalDateTime.now().isAfter(cutoff)) {
            throw new ConflictException("BOOKING_CANCELLATION_NOT_ALLOWED", "Cancellation cutoff has passed");
        }

        List<ReservationSeat> reservationSeats = reservationSeatRepository.findByReservationId(reservationId);
        List<Long> showSeatIds = reservationSeats.stream().map(rs -> rs.getShowSeat().getId()).toList();
        List<ShowSeat> seats = showSeatRepository.lockByShowSeatIds(showSeatIds);
        for (ShowSeat showSeat : seats) {
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeat.setLockedByUser(null);
            showSeat.setLockExpiresAt(null);
        }
        showSeatRepository.saveAll(seats);

        Payment payment = paymentRepository.findByReservationId(reservation.getId()).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        return getBooking(reservationId, userId);
    }

    @Transactional
    public void expireStaleLocks() {
        LocalDateTime now = LocalDateTime.now();

        List<ShowSeat> expiredLocks = showSeatRepository.findByStatusAndLockExpiresAtBefore(ShowSeatStatus.LOCKED, now);
        for (ShowSeat showSeat : expiredLocks) {
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            showSeat.setLockedByUser(null);
            showSeat.setLockExpiresAt(null);
        }
        showSeatRepository.saveAll(expiredLocks);

        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, now);
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
        }
        reservationRepository.saveAll(expiredReservations);
    }
}
