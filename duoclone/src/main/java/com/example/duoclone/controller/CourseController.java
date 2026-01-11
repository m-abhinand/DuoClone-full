package com.example.duoclone.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.duoclone.model.Course;
import com.example.duoclone.model.User;
import com.example.duoclone.model.UserCourse;
import com.example.duoclone.repository.CourseRepository;
import com.example.duoclone.repository.UserCourseRepository;
import com.example.duoclone.repository.UserRepository;

@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;

    public CourseController(CourseRepository courseRepository,
                            UserCourseRepository userCourseRepository,
                            UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.userRepository = userRepository;
    }

    // ✅ Get course details
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable String id, Authentication authentication) {
        Course course = courseRepository.findById(id).orElse(null);
        
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Course not found"));
        }
        
        // Get user enrollment status and progress
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", course.getId());
        response.put("name", course.getName());
        response.put("description", course.getDescription());
        response.put("fullDescription", course.getFullDescription());
        response.put("curriculum", course.getCurriculum());
        response.put("level", course.getLevel() != null ? course.getLevel() : "Beginner to Advanced");
        response.put("duration", course.getDuration() != null ? course.getDuration() : "~8 hours");
        response.put("totalLessons", course.getTotalLessons() != null ? course.getTotalLessons() : 20);
        response.put("totalExercises", course.getTotalExercises() != null ? course.getTotalExercises() : 50);
        
        // Add technical content and MCQ questions
        response.put("technicalContent", course.getTechnicalContent() != null ? course.getTechnicalContent() : new java.util.ArrayList<>());
        response.put("mcqQuestions", course.getMcqQuestions() != null ? course.getMcqQuestions() : new java.util.ArrayList<>());
        
        if (user != null) {
            UserCourse userCourse = userCourseRepository
                .findByUserIdAndCourseId(user.getId(), id)
                .orElse(null);
            
            if (userCourse != null) {
                response.put("enrolled", true);
                response.put("progress", userCourse.getProgress() != null ? userCourse.getProgress() : 0);
                response.put("completed", userCourse.isCompleted());
            } else {
                response.put("enrolled", false);
                response.put("progress", 0);
                response.put("completed", false);
            }
            
            // Add like status
            boolean isLiked = user.getLikedCourses() != null && user.getLikedCourses().contains(id);
            response.put("liked", isLiked);
        } else {
            response.put("enrolled", false);
            response.put("progress", 0);
            response.put("completed", false);
            response.put("liked", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
   @GetMapping
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
   
    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getCourseProgress(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        UserCourse userCourse = userCourseRepository
            .findByUserIdAndCourseId(user.getId(), id)
            .orElse(null);
        
        if (userCourse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Course enrollment not found"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("technicalProgress", userCourse.getTechnicalProgress());
        response.put("mcqProgress", userCourse.getMcqProgress());
        response.put("currentSection", userCourse.getCurrentSection());
        response.put("currentIndex", userCourse.getCurrentIndex());
        response.put("progress", userCourse.getProgress());
        
        System.out.println("Fetched progress for course " + id + ": " + userCourse.getProgress() + "%");
        
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateCourseProgress(
        @PathVariable String id,
        @RequestBody Map<String, Object> progressData,
        Authentication authentication
    ) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        UserCourse userCourse = userCourseRepository
            .findByUserIdAndCourseId(user.getId(), id)
            .orElse(null);
        
        if (userCourse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Course enrollment not found"));
        }
        
        // Update progress fields
        if (progressData.containsKey("progress")) {
            Integer progress = (Integer) progressData.get("progress");
            userCourse.setProgress(progress);
            System.out.println("Updating progress to: " + progress + "%");
        }
        
        if (progressData.containsKey("completed")) {
            userCourse.setCompleted((Boolean) progressData.get("completed"));
        }
        
        if (progressData.containsKey("technicalProgress")) {
            userCourse.setTechnicalProgress((List<Boolean>) progressData.get("technicalProgress"));
        }
        
        if (progressData.containsKey("mcqProgress")) {
            userCourse.setMcqProgress((List<Boolean>) progressData.get("mcqProgress"));
        }
        
        if (progressData.containsKey("currentSection")) {
            userCourse.setCurrentSection((String) progressData.get("currentSection"));
        }
        
        if (progressData.containsKey("currentIndex")) {
            userCourse.setCurrentIndex((Integer) progressData.get("currentIndex"));
        }
        
        userCourse.setLastAccessed(LocalDateTime.now());
        
        // Save to database
        userCourseRepository.save(userCourse);
        
        System.out.println("Progress saved to database for course " + id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Progress updated successfully");
        response.put("progress", userCourse.getProgress());
        
        return ResponseEntity.ok(response);
    }

    // Like/Unlike endpoints
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeCourse(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Course not found"));
        }
        
        // Initialize likedCourses if null
        if (user.getLikedCourses() == null) {
            user.setLikedCourses(new java.util.ArrayList<>());
        }
        
        // Add to liked courses if not already liked
        if (!user.getLikedCourses().contains(id)) {
            user.getLikedCourses().add(id);
            userRepository.save(user);
            System.out.println("User " + userEmail + " liked course: " + course.getName());
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Course liked successfully",
            "liked", true
        ));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeCourse(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        // Remove from liked courses
        if (user.getLikedCourses() != null && user.getLikedCourses().contains(id)) {
            user.getLikedCourses().remove(id);
            userRepository.save(user);
            
            Course course = courseRepository.findById(id).orElse(null);
            String courseName = course != null ? course.getName() : id;
            System.out.println("User " + userEmail + " unliked course: " + courseName);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Course unliked successfully",
            "liked", false
        ));
    }

    @GetMapping("/liked")
    public ResponseEntity<?> getLikedCourses(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        if (user.getLikedCourses() == null || user.getLikedCourses().isEmpty()) {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
        
        // Return set of liked course IDs
        return ResponseEntity.ok(user.getLikedCourses());
    }

    @DeleteMapping("/{id}/unenroll")
    public ResponseEntity<?> unenrollCourse(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "User not found"));
        }
        
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Course not found"));
        }
        
        // Find and delete the user course enrollment
        UserCourse userCourse = userCourseRepository
            .findByUserIdAndCourseId(user.getId(), id)
            .orElse(null);
        
        if (userCourse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "You are not enrolled in this course"));
        }
        
        // Delete the enrollment
        userCourseRepository.delete(userCourse);
        
        System.out.println("User " + userEmail + " unenrolled from course: " + course.getName());
        
        return ResponseEntity.ok(Map.of(
            "message", "Successfully unenrolled from course",
            "courseName", course.getName()
        ));
    }
}
