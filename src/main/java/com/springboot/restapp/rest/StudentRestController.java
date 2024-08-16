package com.springboot.restapp.rest;

import com.springboot.restapp.entity.Student;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentRestController {

    private List<Student> theStudents;

    @PostConstruct
    public void loadData(){
        this.theStudents = new ArrayList<>();
        this.theStudents.add(new Student(1, "John", "Doe"));
        this.theStudents.add(new Student(2, "Daffy", "Munroe"));
        this.theStudents.add(new Student(3, "Charles", "Babbage"));
        this.theStudents.add(new Student(4, "Ryan", "Ivan"));
        this.theStudents.add(new Student(5, "Deva", "Prasanna"));
    }

    @GetMapping("students/{studentId}")
    public Student getStudent(@PathVariable int studentId){
        if ((studentId >= this.theStudents.size()) || (studentId < 0)){
            throw new StudentNotFoundException("Student id not found - " + studentId);
        }
        return theStudents.get(studentId);

    }

    @GetMapping("/students")
    public List<Student>  getStudents(){

        return this.theStudents;
    }

}
