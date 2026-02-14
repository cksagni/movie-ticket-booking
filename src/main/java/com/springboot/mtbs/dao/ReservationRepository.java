package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Reservation;
import com.springboot.mtbs.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime now);
}
