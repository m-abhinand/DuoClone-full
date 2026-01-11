package com.example.duoclone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    private String id;

    private String name;
    private String description;
    private String fullDescription;
    private List<String> curriculum;
    private Integer progress;
    private String level;
    private String duration;
    private Integer totalLessons;
    private Integer totalExercises;
    private Integer likeCount = 0; // Track number of likes
    
    // New fields for detailed course content
    private List<TechnicalContent> technicalContent;
    private List<MCQuestion> mcqQuestions;

    // Nested classes for structured data
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalContent {
        private String title;
        private String content;
        private String codeExample;
        private String language; // e.g., "java", "python", "javascript"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MCQuestion {
        private String question;
        private List<String> options;
        private int correctAnswerIndex; // 0-based index
        private String explanation;
    }

    public Course(String name, String description, String fullDescription, List<String> curriculum) {
        this.name = name;
        this.description = description;
        this.fullDescription = fullDescription;
        this.curriculum = curriculum;
        this.progress = 0;
        this.level = "Beginner to Advanced";
        this.duration = "~8 hours";
        this.totalLessons = 20;
        this.totalExercises = 50;
    }
}
