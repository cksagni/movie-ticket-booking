package com.springboot.restapp.rest;

import com.springboot.restapp.dao.StudentDAO;
import com.springboot.restapp.entity.Student;
import com.springboot.restapp.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentRestController {

    private StudentService studentService;

    @Autowired
    public StudentRestController(StudentService theStudentService){
        this.studentService = theStudentService;
    }

    @GetMapping("/students")
    public List<Student>  getStudents(){
        return this.studentService.findAll();

    }

    @GetMapping("/students/{studentId}")
    public Student getStudent(@PathVariable int studentId){
        Student student = this.studentService.findById(studentId);

        if (student == null){
            throw new RuntimeException("Student id not found - " + studentId);
        }
        return student;
    }

    @PostMapping("/students")
    public Student addStudent(@RequestBody Student student){

        // just in case they pass an id in JSON ... set id to 0
        // to force a save of new item...instead of update

        student.setId(0);
        return this.studentService.save(student);
    }

    @PutMapping("/students")
    public Student updateStudent(@RequestBody Student student){
        return this.studentService.save(student);
    }

    @DeleteMapping("/students/{studentId}")
    public String deleteStudent(@PathVariable int studentId){
        Student student = this.studentService.findById(studentId);

        if (student == null){
            throw new RuntimeException("Student doesn't exist with id - " + studentId);
        }
        this.studentService.deleteById(studentId);
        return "Deleted student id - " + studentId;
    }

}
