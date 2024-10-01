package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StateRepository extends JpaRepository<State, Integer> {
}
