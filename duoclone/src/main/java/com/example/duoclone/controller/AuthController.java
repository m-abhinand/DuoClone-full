package com.example.duoclone.controller;

import com.example.duoclone.model.User;
import com.example.duoclone.repository.UserRepository;
import com.example.duoclone.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate input
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                response.put("error", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getPassword() == null || user.getPassword().length() < 6) {
                response.put("error", "Password must be at least 6 characters");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getName() == null || user.getName().isBlank()) {
                response.put("error", "Name is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                response.put("error", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Check if this is an admin registration
            // If name is "admin" (case-insensitive) AND email contains "admin"
            if (user.getName().trim().equalsIgnoreCase("admin") && 
                user.getEmail().toLowerCase().contains("admin")) {
                user.setRole("ADMIN");
                response.put("message", "Admin user registered successfully");
            } else {
                user.setRole("USER");
                response.put("message", "User registered successfully");
            }
            
            // Set the createdAt timestamp
            user.setCreatedAt(java.time.LocalDateTime.now());

            User savedUser = userRepository.save(user);

            response.put("userId", savedUser.getId());
            response.put("role", savedUser.getRole());
            response.put("createdAt", savedUser.getCreatedAt().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();

        try {
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("\n========== LOGIN ATTEMPT ==========");
            System.out.println("Email: " + email);
            System.out.println("Password length: " + (password != null ? password.length() : 0));

            // Validate input
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                System.out.println("❌ Validation failed: Empty email or password");
                response.put("error", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if any users exist
            long userCount = userRepository.count();
            System.out.println("📊 Total users in database: " + userCount);
            
            // List all user emails for debugging
            System.out.println("📋 All user emails in database:");
            userRepository.findAll().forEach(u -> System.out.println("   - " + u.getEmail()));

            // Find user by email
            System.out.println("🔍 Searching for user: " + email);
            Optional<User> userOptional = userRepository.findByEmail(email);
            System.out.println("🔍 Search result - Present: " + userOptional.isPresent());
            
            User user = userOptional.orElse(null);

            if (user == null) {
                System.out.println("❌ User not found: " + email);
                System.out.println("   Trying alternative search methods...");
                
                // Try finding all users and match manually
                List<User> allUsers = userRepository.findAll();
                for (User u : allUsers) {
                    if (u.getEmail() != null && u.getEmail().equals(email)) {
                        System.out.println("✅ Found user via manual search!");
                        user = u;
                        break;
                    }
                }
                
                if (user == null) {
                    response.put("error", "Invalid email or password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }

            System.out.println("✅ User found in database");
            System.out.println("   User ID: " + user.getId());
            System.out.println("   User name: " + user.getName());
            System.out.println("   User role: " + user.getRole());
            System.out.println("   Password hash (first 20 chars): " + 
                (user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) : "NULL"));

            // Verify password
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("   Password match result: " + passwordMatches);
            
            if (!passwordMatches) {
                System.out.println("❌ Password verification failed");
                response.put("error", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            System.out.println("✅ Password verified successfully");

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            System.out.println("✅ JWT token generated");
            System.out.println("   Token (first 50 chars): " + token.substring(0, Math.min(50, token.length())));

            response.put("token", token);
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

            System.out.println("✅ Login successful for: " + email);
            System.out.println("====================================\n");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                response.put("valid", false);
                return ResponseEntity.ok(response);
            }

            String jwt = token.substring(7);
            boolean isValid = jwtUtil.validateToken(jwt);

            if (isValid) {
                String email = jwtUtil.getEmailFromToken(jwt);
                String role = jwtUtil.getRoleFromToken(jwt);
                response.put("valid", true);
                response.put("email", email);
                response.put("role", role);
            } else {
                response.put("valid", false);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
