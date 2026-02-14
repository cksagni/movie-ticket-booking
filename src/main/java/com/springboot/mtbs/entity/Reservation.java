package com.springboot.mtbs.entity;

import com.springboot.mtbs.entity.enums.ReservationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "ticket_ref")
    private String ticketRef;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> reservationSeats = new ArrayList<>();

    public Reservation() {}

    public Reservation(User user, Show show, LocalDateTime reservationTime, LocalDateTime expiresAt, Double totalAmount) {
        this.user = user;
        this.show = show;
        this.reservationTime = reservationTime;
        this.expiresAt = expiresAt;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Show getShow() { return show; }
    public void setShow(Show show) { this.show = show; }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTicketRef() {
        return ticketRef;
    }

    public void setTicketRef(String ticketRef) {
        this.ticketRef = ticketRef;
    }

    public LocalDateTime getReservationTime() { return reservationTime; }
    public void setReservationTime(LocalDateTime reservationTime) { this.reservationTime = reservationTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ReservationSeat> getReservationSeats() {
        return reservationSeats;
    }

    public void setReservationSeats(List<ReservationSeat> reservationSeats) {
        this.reservationSeats = reservationSeats;
    }
}
