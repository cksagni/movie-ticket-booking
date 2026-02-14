package com.springboot.mtbs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation_seats")
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public ShowSeat getShowSeat() {
        return showSeat;
    }

    public void setShowSeat(ShowSeat showSeat) {
        this.showSeat = showSeat;
    }
}
