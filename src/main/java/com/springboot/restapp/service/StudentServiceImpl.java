package com.springboot.restapp.service;

import com.springboot.restapp.dao.StudentRepository;
import com.springboot.restapp.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService{

    private StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepo){
        this.studentRepository = studentRepo;
    }

    @Override
    public List<Student> findAll() {
        return this.studentRepository.findAll();
    }

    @Override
    public Student findById(int id) {
        Optional<Student> result = this.studentRepository.findById(id);
        Student student = null;
        if (result.isPresent()){
            student = result.get();
        }
        else{
            throw new RuntimeException("Did not find student id - " + id);
        }
        return student;
    }

    @Override
    public Student save(Student student) {
        return this.studentRepository.save(student);
    }

    @Override
    public void deleteById(int id) {
        this.studentRepository.deleteById(id);
    }
}