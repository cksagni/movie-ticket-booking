package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    List<ReservationSeat> findByReservationId(Long reservationId);
}
