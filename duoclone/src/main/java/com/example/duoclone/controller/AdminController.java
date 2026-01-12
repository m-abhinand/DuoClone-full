package com.example.duoclone.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.duoclone.model.Course;
import com.example.duoclone.model.User;
import com.example.duoclone.model.UserCourse;
import com.example.duoclone.model.UserProgress;
import com.example.duoclone.repository.CourseRepository;
import com.example.duoclone.repository.UserCourseRepository;
import com.example.duoclone.repository.UserProgressRepository;
import com.example.duoclone.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserProgressRepository userProgressRepository;

    public AdminController(UserRepository userRepository,
                          CourseRepository courseRepository,
                          UserCourseRepository userCourseRepository,
                          UserProgressRepository userProgressRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.userCourseRepository = userCourseRepository;
        this.userProgressRepository = userProgressRepository;
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
            
            // Basic user info
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            
            // Format created at date
            if (user.getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                response.put("createdAt", user.getCreatedAt().format(formatter));
                response.put("memberSince", user.getCreatedAt().format(formatter));
            }
            
            // Get enrolled courses data
            List<UserCourse> userCourses = userCourseRepository.findByUserId(user.getId());
            long completedCount = userCourses.stream().filter(UserCourse::isCompleted).count();
            
            response.put("enrolledCourses", userCourses.size());
            response.put("completedCourses", (int) completedCount);
            response.put("activeCourses", userCourses.size() - (int) completedCount);
            
            // Get detailed course information with progress
            List<Map<String, Object>> courseDetails = new ArrayList<>();
            int totalTechnicalCompleted = 0;
            int totalTechnicalModules = 0;
            int totalMcqCompleted = 0;
            int totalMcqQuestions = 0;
            
            for (UserCourse userCourse : userCourses) {
                Course course = courseRepository.findById(userCourse.getCourseId()).orElse(null);
                if (course != null) {
                    Map<String, Object> courseDetail = new HashMap<>();
                    courseDetail.put("courseId", course.getId());
                    courseDetail.put("courseName", course.getName());
                    courseDetail.put("courseLevel", course.getLevel());
                    courseDetail.put("progress", userCourse.getProgress());
                    courseDetail.put("completed", userCourse.isCompleted());
                    
                    // Format enrollment date
                    if (userCourse.getEnrolledAt() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                        courseDetail.put("enrolledAt", userCourse.getEnrolledAt().format(formatter));
                    }
                    
                    // Format last accessed date
                    if (userCourse.getLastAccessed() != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                        courseDetail.put("lastAccessed", userCourse.getLastAccessed().format(formatter));
                    }
                    
                    // Technical content progress
                    List<Boolean> techProgress = userCourse.getTechnicalProgress();
                    int techTotal = course.getTechnicalContent() != null ? course.getTechnicalContent().size() : 0;
                    int techCompleted = (int) techProgress.stream().filter(b -> b).count();
                    courseDetail.put("technicalModulesTotal", techTotal);
                    courseDetail.put("technicalModulesCompleted", techCompleted);
                    courseDetail.put("technicalProgress", techProgress);
                    
                    totalTechnicalModules += techTotal;
                    totalTechnicalCompleted += techCompleted;
                    
                    // MCQ progress
                    List<Boolean> mcqProgressList = userCourse.getMcqProgress();
                    int mcqTotal = course.getMcqQuestions() != null ? course.getMcqQuestions().size() : 0;
                    int mcqCompleted = (int) mcqProgressList.stream().filter(b -> b).count();
                    courseDetail.put("mcqQuestionsTotal", mcqTotal);
                    courseDetail.put("mcqQuestionsCompleted", mcqCompleted);
                    courseDetail.put("mcqProgress", mcqProgressList);
                    
                    totalMcqQuestions += mcqTotal;
                    totalMcqCompleted += mcqCompleted;
                    
                    // Current section and position
                    courseDetail.put("currentSection", userCourse.getCurrentSection());
                    courseDetail.put("currentIndex", userCourse.getCurrentIndex());
                    
                    courseDetails.add(courseDetail);
                }
            }
            
            response.put("courseDetails", courseDetails);
            
            // Overall statistics
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalTechnicalModules", totalTechnicalModules);
            statistics.put("completedTechnicalModules", totalTechnicalCompleted);
            statistics.put("totalMcqQuestions", totalMcqQuestions);
            statistics.put("completedMcqQuestions", totalMcqCompleted);
            
            // Calculate overall completion percentage
            int totalItems = totalTechnicalModules + totalMcqQuestions;
            int completedItems = totalTechnicalCompleted + totalMcqCompleted;
            double overallCompletion = totalItems > 0 ? (completedItems * 100.0 / totalItems) : 0;
            statistics.put("overallCompletionPercentage", Math.round(overallCompletion));
            
            response.put("statistics", statistics);
            
            // Get liked courses
            List<String> likedCourseIds = user.getLikedCourses();
            List<Map<String, Object>> likedCourses = new ArrayList<>();
            if (likedCourseIds != null) {
                for (String courseId : likedCourseIds) {
                    Course course = courseRepository.findById(courseId).orElse(null);
                    if (course != null) {
                        Map<String, Object> likedCourse = new HashMap<>();
                        likedCourse.put("courseId", course.getId());
                        likedCourse.put("courseName", course.getName());
                        likedCourse.put("courseLevel", course.getLevel());
                        likedCourse.put("courseDescription", course.getDescription());
                        likedCourses.add(likedCourse);
                    }
                }
            }
            response.put("likedCourses", likedCourses);
            
            // Get user progress data (game progress, points, level)
            List<UserProgress> progressList = userProgressRepository.findByUserId(user.getId());
            List<Map<String, Object>> gameProgress = new ArrayList<>();
            int totalPoints = 0;
            int averageLevel = 0;
            
            for (UserProgress progress : progressList) {
                Course course = courseRepository.findById(progress.getCourseId()).orElse(null);
                if (course != null) {
                    Map<String, Object> progressDetail = new HashMap<>();
                    progressDetail.put("courseId", course.getId());
                    progressDetail.put("courseName", course.getName());
                    progressDetail.put("level", progress.getLevel());
                    progressDetail.put("points", progress.getPoints());
                    progressDetail.put("knownTermsCount", progress.getKnownTerms().size());
                    gameProgress.add(progressDetail);
                    
                    totalPoints += progress.getPoints();
                    averageLevel += progress.getLevel();
                }
            }
            
            if (!progressList.isEmpty()) {
                averageLevel = averageLevel / progressList.size();
            }
            
            Map<String, Object> gameStats = new HashMap<>();
            gameStats.put("totalPoints", totalPoints);
            gameStats.put("averageLevel", averageLevel);
            gameStats.put("coursesWithProgress", progressList.size());
            gameStats.put("details", gameProgress);
            
            response.put("gameProgress", gameStats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to get user: " + e.getMessage());
            e.printStackTrace();
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
