package com.springboot.restapp.dao;

import com.springboot.restapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

// 'students' is default, for any other case it can be specified exclusively
@RepositoryRestResource(path = "students")
public interface StudentRepository extends JpaRepository<Student, Integer> {
    // no need to write any code
}
