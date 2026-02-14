package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);

    List<Seat> findByScreenIdAndIdIn(Long screenId, List<Long> seatIds);
}
