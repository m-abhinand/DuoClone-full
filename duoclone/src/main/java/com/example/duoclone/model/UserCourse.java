package com.example.duoclone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_courses")
public class UserCourse {

    @Id
    private String id;

    private String userId;
    private String courseId;
    private Integer progress;
    private boolean completed;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessed;
    
    // New fields for detailed progress
    private List<Boolean> technicalProgress;
    private List<Boolean> mcqProgress;
    private String currentSection;
    private Integer currentIndex;

    public UserCourse() {
        this.enrolledAt = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
        this.progress = 0;
        this.completed = false;
        this.technicalProgress = new ArrayList<>();
        this.mcqProgress = new ArrayList<>();
        this.currentSection = "technical";
        this.currentIndex = 0;
    }

    public UserCourse(String userId, String courseId) {
        this();
        this.userId = userId;
        this.courseId = courseId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public List<Boolean> getTechnicalProgress() {
        return technicalProgress != null ? technicalProgress : new ArrayList<>();
    }

    public void setTechnicalProgress(List<Boolean> technicalProgress) {
        this.technicalProgress = technicalProgress;
    }

    public List<Boolean> getMcqProgress() {
        return mcqProgress != null ? mcqProgress : new ArrayList<>();
    }

    public void setMcqProgress(List<Boolean> mcqProgress) {
        this.mcqProgress = mcqProgress;
    }

    public String getCurrentSection() {
        return currentSection;
    }

    public void setCurrentSection(String currentSection) {
        this.currentSection = currentSection;
    }

    public Integer getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(Integer currentIndex) {
        this.currentIndex = currentIndex;
    }
}
