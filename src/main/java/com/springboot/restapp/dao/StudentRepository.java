package com.springboot.restapp.dao;

import com.springboot.restapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    // no need to write any code
}
