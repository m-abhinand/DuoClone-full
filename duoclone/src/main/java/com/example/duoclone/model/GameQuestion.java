package com.example.duoclone.model;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "game_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameQuestion {
    @Id
    private String id;

    private String category;
    private String question;
    private String codeSnippet;
    private List<String> options;
    private String correctAnswer;
    private int difficulty;
}
