package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Integer> {
    List<Country> findByIsActiveTrue();
}
