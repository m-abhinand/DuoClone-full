package com.example.duoclone.repository;
import com.example.duoclone.model.GameQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GameQuestionRepository
            extends MongoRepository<GameQuestion, String> {

        List<GameQuestion> findByCategory(String category);
    }

