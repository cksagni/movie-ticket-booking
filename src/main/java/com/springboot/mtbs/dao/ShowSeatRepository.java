package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.ShowSeat;
import com.springboot.mtbs.entity.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.show.id = :showId and ss.seat.id in :seatIds")
    List<ShowSeat> lockSeatsForUpdate(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.id in :showSeatIds")
    List<ShowSeat> lockByShowSeatIds(@Param("showSeatIds") List<Long> showSeatIds);

    List<ShowSeat> findByStatusAndLockExpiresAtBefore(ShowSeatStatus status, LocalDateTime now);
}
