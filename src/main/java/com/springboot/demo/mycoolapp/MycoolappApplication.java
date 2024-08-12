package com.springboot.demo.mycoolapp;

import com.springboot.demo.mycoolapp.dao.StudentDAO;
import com.springboot.demo.mycoolapp.entity.Student;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(
		scanBasePackages = {
				"com.springboot.demo.mycoolapp",
				"com.springboot.demo.util"
		})
public class MycoolappApplication {

	public static void main(String[] args) {

		SpringApplication.run(MycoolappApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(StudentDAO studentDAO){
		return runner -> {
//			create students
//			createMultipleStudent(studentDAO);

//			find student by id
//			readStudent(studentDAO);

//			find all Students
//			queryForStudents(studentDAO);

//			find student by last name
//			queryForStudentsByLastName(studentDAO);

//			update the student
			updateStudent(studentDAO);
//			find 
		};
	}

	private void updateStudent(StudentDAO studentDAO) {
		Student student = studentDAO.findById(1);
		student.setFirstName("Scooby");
		studentDAO.update(student);
		System.out.println("Updated Student: " + student);
	}

	private void queryForStudentsByLastName(StudentDAO studentDAO) {
		List<Student> theStudents = studentDAO.findStudentByLastName("Doe");

		for(Student student: theStudents){
			System.out.println(student);
		}
	}

	private void queryForStudents(StudentDAO studentDAO) {
		List<Student> theStudents = studentDAO.findAll();

		for(Student student: theStudents){
			System.out.println(student);
		}

	}

	private void readStudent(StudentDAO studentDAO) {

		Student student = studentDAO.findById(1);
		System.out.println("Details of student with id 1: " + student);
	}

	private void createMultipleStudent(StudentDAO studentDAO) {
		System.out.println("Creating 3 student objects...");
		Student tempStudent1 = new Student("John", "Doe", "john@xyz.com");
		Student tempStudent2 = new Student("Jamie", "Reagan", "jamie@xyz.com");
		Student tempStudent3 = new Student("Ryan", "Ivan", "ryan@xyz.com");

		System.out.println("Saving the Students...");
		studentDAO.save(tempStudent1);
		studentDAO.save(tempStudent2);
		studentDAO.save(tempStudent3);

		System.out.println("Saved Student. Generated id: " + tempStudent1.getId());
		System.out.println("Saved Student. Generated id: " + tempStudent2.getId());
		System.out.println("Saved Student. Generated id: " + tempStudent3.getId());
	}

}
