package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Country;
import com.springboot.mtbs.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {
    List<State> findByIsActiveTrue();
    @Query("SELECT s FROM State s WHERE s.countryId = :countryId AND s.isActive = true")
    List<State> findAllActiveStatesOfACountry(@Param("countryId") Integer countryId);

    List<State> findAllStatesOfACountry(Integer countryId);

    @Modifying
    @Query("UPDATE State s SET s.isActive = false WHERE s.country.id = :countryId")
    void deactivateStatesByCountryId(@Param("countryId") Integer countryId);
}
