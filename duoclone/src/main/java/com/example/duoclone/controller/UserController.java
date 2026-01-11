package com.example.duoclone.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;

    public UserController(CourseRepository courseRepository, 
                         UserCourseRepository userCourseRepository,
                         UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public List<CourseDTO> userDashboard(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return List.of();
        }
        
        // Get all courses
        List<Course> allCourses = courseRepository.findAll();
        
        // Get user's enrolled courses
        List<UserCourse> userCourses = userCourseRepository.findByUserId(user.getId());
        Map<String, Integer> progressMap = userCourses.stream()
            .collect(Collectors.toMap(
                UserCourse::getCourseId,
                uc -> uc.getProgress() != null ? uc.getProgress() : 0
            ));
        
        // Map to DTOs with progress
        return allCourses.stream().map(course -> new CourseDTO(
            course.getId(),
            course.getName(),
            course.getDescription(),
            progressMap.getOrDefault(course.getId(), 0)
        )).collect(Collectors.toList());
    }

    @GetMapping("/enrolled-courses")
    public List<CourseDTO> getEnrolledCourses(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        
        if (user == null) {
            return List.of();
        }
        
        // Get only user's enrolled courses
        List<UserCourse> userCourses = userCourseRepository.findByUserId(user.getId());
        
        // Fetch course details for each enrolled course
        return userCourses.stream()
            .map(uc -> {
                Course course = courseRepository.findById(uc.getCourseId()).orElse(null);
                if (course != null) {
                    return new CourseDTO(
                        course.getId(),
                        course.getName(),
                        course.getDescription(),
                        uc.getProgress() != null ? uc.getProgress() : 0
                    );
                }
                return null;
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollCourse(@PathVariable String courseId, 
                                         Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Check if course exists
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                response.put("error", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Check if already enrolled
            if (userCourseRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
                response.put("message", "Already enrolled in this course");
                response.put("courseId", courseId);
                return ResponseEntity.ok(response);
            }
            
            // Create enrollment
            UserCourse userCourse = new UserCourse(user.getId(), courseId);
            userCourseRepository.save(userCourse);
            
            response.put("message", "Successfully enrolled in course");
            response.put("courseId", courseId);
            response.put("courseName", course.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("error", "Enrollment failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/update-progress/{courseId}")
    public ResponseEntity<?> updateProgress(@PathVariable String courseId,
                                           @RequestBody Map<String, Integer> request,
                                           Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElse(null);
            
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Integer progress = request.get("progress");
            if (progress == null || progress < 0 || progress > 100) {
                response.put("error", "Invalid progress value");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserCourse userCourse = userCourseRepository
                .findByUserIdAndCourseId(user.getId(), courseId)
                .orElse(null);
            
            if (userCourse == null) {
                response.put("error", "Not enrolled in this course");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            userCourse.setProgress(progress);
            if (progress >= 100) {
                userCourse.setCompleted(true);
            }
            userCourseRepository.save(userCourse);
            
            response.put("message", "Progress updated successfully");
            response.put("progress", progress.toString());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Update failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Inner DTO class
    public static class CourseDTO {
        public String id;
        public String name;
        public String description;
        public int progress;

        public CourseDTO(String id, String name, String description, int progress) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.progress = progress;
        }
    }
}
