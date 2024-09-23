package com.springboot.mtbs.dao;

import com.springboot.mtbs.entity.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "theatre")
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
}
