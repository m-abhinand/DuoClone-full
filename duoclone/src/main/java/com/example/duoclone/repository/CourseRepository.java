package com.example.duoclone.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.duoclone.model.Course;

public interface CourseRepository extends MongoRepository<Course, String> {
}
