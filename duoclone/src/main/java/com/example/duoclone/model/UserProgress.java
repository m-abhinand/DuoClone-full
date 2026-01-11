package com.example.duoclone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "user_progress")
public class UserProgress {

    @Id
    private String id;

    private String userId;
    private String courseId;

    private int level = 1;
    private int points = 0;

    private boolean completed;
    private LocalDateTime completedAt;

    private final List<String> knownTerms = new ArrayList<>();

    // ---- Constructors ----
    public UserProgress() {}

    // ---- Getters & Setters ----
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getLevel() {
        return level;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public List<String> getKnownTerms() {
        return Collections.unmodifiableList(knownTerms);
    }

    // ---- Business Logic ----
    public void addPoints(int p) {
        if (p <= 0) return;

        points += p;

        if (points >= 100) {
            level += points / 100;
            points = points % 100;
        }
    }

    public void addKnownTerm(String termId) {
        if (termId != null && !termId.isBlank() && !knownTerms.contains(termId)) {
            knownTerms.add(termId);
        }
    }
}
