package com.example.duoclone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.duoclone.model.User;
import com.example.duoclone.repository.UserRepository;

@SpringBootApplication
public class DuocloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(DuocloneApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository, 
									PasswordEncoder passwordEncoder,
									MongoTemplate mongoTemplate) {
		return args -> {
			System.out.println("\n==========================================");
			System.out.println("🔌 MONGODB CONNECTION CHECK");
			System.out.println("==========================================");
			
			// Show which database we're connected to
			String dbName = mongoTemplate.getDb().getName();
			System.out.println("📁 Connected to database: " + dbName);
			System.out.println("📊 Collections in database:");
			mongoTemplate.getDb().listCollectionNames().forEach(name -> 
				System.out.println("   - " + name)
			);
			
			System.out.println("\n==========================================");
			System.out.println("📋 USER DATA CHECK");
			System.out.println("==========================================\n");
			
			// Check all users
			java.util.List<User> allUsers = userRepository.findAll();
			System.out.println("Total users found: " + allUsers.size());
			
			if (allUsers.isEmpty()) {
				System.out.println("\n⚠️  No users found in database!");
				System.out.println("Creating test users...\n");
				
				// Create test regular user
				User testUser = new User();
				testUser.setName("Test User");
				testUser.setEmail("test@test.com");
				testUser.setPassword(passwordEncoder.encode("password"));
				testUser.setRole("USER");
				userRepository.save(testUser);
				System.out.println("✅ Created test user: test@test.com / password");
				
				// Create test admin user
				User adminUser = new User();
				adminUser.setName("Admin");
				adminUser.setEmail("admin@admin.com");
				adminUser.setPassword(passwordEncoder.encode("admin123"));
				adminUser.setRole("ADMIN");
				userRepository.save(adminUser);
				System.out.println("✅ Created admin user: admin@admin.com / admin123");
				
				// Reload all users
				allUsers = userRepository.findAll();
			}
			
			// Display all users
			for (User user : allUsers) {
				System.out.println("\n👤 User: " + user.getEmail());
				System.out.println("   ID: " + user.getId());
				System.out.println("   Name: " + user.getName());
				System.out.println("   Role: " + user.getRole());
				System.out.println("   Password (first 20 chars): " + 
					(user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) : "NULL"));
				System.out.println("   Is BCrypt?: " + (user.getPassword() != null && user.getPassword().startsWith("$2")));
				
				// Fix passwords that aren't BCrypt encoded
				if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
					System.out.println("   ⚠️  Password is NOT BCrypt encoded! Fixing...");
					String rawPassword = user.getPassword();
					user.setPassword(passwordEncoder.encode(rawPassword));
					userRepository.save(user);
					System.out.println("   ✅ Password has been re-encoded with BCrypt");
				}
			}
			
			System.out.println("\n==========================================");
			System.out.println("✅ INITIALIZATION COMPLETE");
			System.out.println("==========================================\n");
		};
	}
}
