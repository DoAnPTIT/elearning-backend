package com.doanptit.elearing_backend_service.configuration;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin1@elearning.com";
    private static final String ADMIN_PASSWORD_RAW = "admin123";

    @Override
    public void run(String... args) throws Exception {
        Optional<User> existingAdmin = userRepository.findByEmail(ADMIN_EMAIL);

        if (existingAdmin.isEmpty()) {

            String hashedPassword = passwordEncoder.encode(ADMIN_PASSWORD_RAW);

            User admin = User.builder()
                    .email(ADMIN_EMAIL)
                    .password(hashedPassword)
                    .firstname("System")
                    .lastname("Admin")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);

            System.out.println("✅ ADMIN user created successfully.");
            System.out.println("   Email: " + ADMIN_EMAIL);
            System.out.println("   Password: " + ADMIN_PASSWORD_RAW);
            System.out.println("   Hashed Password (DB): " + hashedPassword.substring(0, 30) + "...");

        } else {
            System.out.println("ℹ️ ADMIN user already exists. Skipping creation.");

            User admin = existingAdmin.get();
            boolean match = passwordEncoder.matches(ADMIN_PASSWORD_RAW, admin.getPassword());
            System.out.println("   Current password match test: " + match);

            if (!match) {
                String newHash = passwordEncoder.encode(ADMIN_PASSWORD_RAW);
                admin.setPassword(newHash);
                userRepository.save(admin);
                System.out.println("   ⚠️ WARNING: Password was mismatched. Updated hash in DB with new hash.");
            }
        }
    }
}