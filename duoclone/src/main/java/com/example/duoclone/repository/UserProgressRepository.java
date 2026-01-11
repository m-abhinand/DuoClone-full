package com.example.duoclone.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.duoclone.model.UserProgress;

public interface UserProgressRepository
        extends MongoRepository<UserProgress, String> {

    List<UserProgress> findByUserId(String userId);

}
