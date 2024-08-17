package com.springboot.restapp.dao;

import com.springboot.restapp.entity.Student;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentDAOJpaImpl implements StudentDAO{

    private EntityManager entityManager;

    @Autowired
    public StudentDAOJpaImpl(EntityManager theEntityManager){
        this.entityManager = theEntityManager;
    }


    @Override
    public List<Student> findAll() {

        TypedQuery<Student> query = this.entityManager.createQuery("FROM Student", Student.class);
        return query.getResultList();
    }

    @Override
    public Student findById(int id) {
        return this.entityManager.find(Student.class, id);
    }

    @Override
    public Student save(Student student) {
        return this.entityManager.merge(student);
    }

    @Override
    public void deleteById(int id) {
        Student student = entityManager.find(Student.class, id);
        this.entityManager.remove(student);
    }

}
