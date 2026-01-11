package com.example.duoclone.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duoclone.model.Course;
import com.example.duoclone.model.User;
import com.example.duoclone.repository.CourseRepository;
import com.example.duoclone.repository.UserCourseRepository;
import com.example.duoclone.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;

    public AdminController(UserRepository userRepository,
                          CourseRepository courseRepository,
                          UserCourseRepository userCourseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "Welcome ADMIN";
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            long enrolledCount = userCourseRepository.countByUserId(user.getId());
            long completedCount = userCourseRepository.countByUserIdAndCompleted(user.getId(), true);
            return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                (int) enrolledCount,
                (int) completedCount
            );
        }).collect(Collectors.toList());
    }

    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate course data
            if (course.getName() == null || course.getName().isBlank()) {
                response.put("error", "Course name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (course.getDescription() == null || course.getDescription().isBlank()) {
                response.put("error", "Course description is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save course
            Course savedCourse = courseRepository.save(course);
            
            response.put("message", "Course created successfully");
            response.put("courseId", savedCourse.getId());
            response.put("courseName", savedCourse.getName());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to create course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable String id, @RequestBody Course course) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Course existingCourse = courseRepository.findById(id).orElse(null);
            
            if (existingCourse == null) {
                response.put("error", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Update course fields
            existingCourse.setName(course.getName());
            existingCourse.setDescription(course.getDescription());
            existingCourse.setFullDescription(course.getFullDescription());
            existingCourse.setLevel(course.getLevel());
            existingCourse.setDuration(course.getDuration());
            existingCourse.setTotalLessons(course.getTotalLessons());
            existingCourse.setTotalExercises(course.getTotalExercises());
            existingCourse.setCurriculum(course.getCurriculum());
            existingCourse.setTechnicalContent(course.getTechnicalContent());
            existingCourse.setMcqQuestions(course.getMcqQuestions());
            
            courseRepository.save(existingCourse);
            
            response.put("message", "Course updated successfully");
            response.put("courseId", existingCourse.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to update course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            if (!courseRepository.existsById(id)) {
                response.put("error", "Course not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            courseRepository.deleteById(id);
            response.put("message", "Course deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to delete course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findById(id).orElse(null);
            
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            long enrolledCount = userCourseRepository.countByUserId(user.getId());
            long completedCount = userCourseRepository.countByUserIdAndCompleted(user.getId(), true);
            
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("enrolledCourses", (int) enrolledCount);
            response.put("completedCourses", (int) completedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to get user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            User user = userRepository.findById(id).orElse(null);
            
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Prevent deleting admin users
            if ("ADMIN".equals(user.getRole()) || "ROLE_ADMIN".equals(user.getRole())) {
                response.put("error", "Cannot delete admin users");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Delete all user course enrollments first
            userCourseRepository.deleteByUserId(id);
            
            // Delete user
            userRepository.deleteById(id);
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DTO classes
    public static class UserDTO {
        public String id;
        public String name;
        public String email;
        public String role;
        public int enrolledCourses;
        public int completedCourses;

        public UserDTO(String id, String name, String email, String role, 
                      int enrolledCourses, int completedCourses) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.enrolledCourses = enrolledCourses;
            this.completedCourses = completedCourses;
        }
    }
}
