package com.springboot.restapp.service;

import com.springboot.restapp.entity.Student;

import java.util.List;

public interface StudentService {

    List<Student> findAll();

    Student findById(int id);

    Student save(Student student);

    void deleteById(int id);
}
