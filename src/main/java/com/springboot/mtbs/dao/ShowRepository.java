package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("select s from Show s where s.movie.id = :movieId and s.showTime >= :start and s.showTime < :end")
    List<Show> findShowsByMovieAndDate(@Param("movieId") Long movieId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
