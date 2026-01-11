package com.example.duoclone.service;

import com.example.duoclone.model.GameQuestion;
import com.example.duoclone.repository.GameQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class GameService {

    private final GameQuestionRepository repository;

    public GameService(GameQuestionRepository repository) {
        this.repository = repository;
    }

    public List<GameQuestion> startTest(String category) {

        List<GameQuestion> questions =
                repository.findByCategory(category);

        Collections.shuffle(questions);

        List<GameQuestion> testQuestions =
                questions.stream().limit(15).toList();

        testQuestions.forEach(q ->
                Collections.shuffle(q.getOptions()));

        return testQuestions;
    }
}
