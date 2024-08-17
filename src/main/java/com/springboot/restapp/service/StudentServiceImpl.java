package com.springboot.restapp.service;

import com.springboot.restapp.dao.StudentDAO;
import com.springboot.restapp.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService{

    private StudentDAO studentDAO;

    @Autowired
    public StudentServiceImpl(StudentDAO theStudentDAO){
        this.studentDAO = theStudentDAO;
    }

    @Override
    public List<Student> findAll() {
        return this.studentDAO.findAll();
    }

    @Override
    public Student findById(int id) {
        return this.studentDAO.findById(id);
    }

    @Transactional
    @Override
    public Student save(Student student) {
        return this.studentDAO.save(student);
    }

    @Transactional
    @Override
    public void deleteById(int id) {
        this.studentDAO.deleteById(id);
    }
}
