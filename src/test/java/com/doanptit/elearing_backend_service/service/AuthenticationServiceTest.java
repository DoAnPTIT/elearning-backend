package com.doanptit.elearing_backend_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthenticationServiceTest {
    @Test
    void testPassWord(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$X9D9oCsErm6k3NfO4z9F.ezE4D8VvDP1E3iODXyLQ1.5ZrD3vG3yW";
        System.out.println(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testGenerateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String rawPassword = "admin123";
        String encoded = encoder.encode(rawPassword);
        System.out.println("Hash mới: " + encoded);
        System.out.println("Khớp với hash cũ: " + encoder.matches(rawPassword, "$2a$10$X9D9oCsErm6k3NfO4z9F.ezE4D8VvDP1E3iODXyLQ1.5ZrD3vG3yW"));
    }

    @Test
    void testGenerateNewHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("test123"));
    }
}

