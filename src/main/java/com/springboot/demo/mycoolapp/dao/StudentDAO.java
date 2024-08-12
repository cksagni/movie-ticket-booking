package com.springboot.demo.mycoolapp.dao;

import com.springboot.demo.mycoolapp.entity.Student;

import java.util.List;

public interface StudentDAO {
    void save(Student student);

    Student findById(Integer id);

    List<Student> findAll();

    List<Student> findStudentByLastName(String lastName);

    void update(Student student);
}
