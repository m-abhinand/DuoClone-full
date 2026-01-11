package com.example.duoclone.config;

import com.example.duoclone.model.GameQuestion;
import com.example.duoclone.repository.GameQuestionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameDataLoader implements CommandLineRunner {

    private final GameQuestionRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        if (repository.count() > 0) {
            System.out.println("✅ Questions already exist");
            return;
        }

        load("data/java.json");
//        load("data/python.json");
//        load("data/javascript.json");
//        load("data/react.json");
//        load("data/springboot.json");

        System.out.println("✅ Game questions loaded into MongoDB");
    }

    private void load(String path) throws Exception {

        InputStream is =
                new ClassPathResource(path).getInputStream();

        List<GameQuestion> questions =
                objectMapper.readValue(
                        is,
                        new TypeReference<>() {}
                );

        repository.saveAll(questions);
    }
}
