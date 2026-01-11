package com.example.duoclone.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.duoclone.model.UserCourse;

import java.util.List;
import java.util.Optional;

public interface UserCourseRepository extends MongoRepository<UserCourse, String> {
    List<UserCourse> findByUserId(String userId);
    Optional<UserCourse> findByUserIdAndCourseId(String userId, String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
    long countByUserId(String userId);
    long countByUserIdAndCompleted(String userId, boolean completed);
    void deleteByUserId(String userId);
}
