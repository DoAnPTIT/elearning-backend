package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.configuration.JwtUtil;
import com.doanptit.elearing_backend_service.dto.req.LoginRequest;
import com.doanptit.elearing_backend_service.dto.res.LoginResponse;
import com.doanptit.elearing_backend_service.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class AuthenticationServiceTest {

    @Test
    void login_success() {
        // Arrange
        AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwtUtil = new JwtUtil();
        jwtUtil.setSecret("defaultSecretKey123456789012345678901234567890");
        jwtUtil.setExpiration(3600000); // 1h

        User fakeUser = User.builder()
                .id(1L)
                .email("admin@elearning.com")
                .password("encodedPassword")
                .role(com.doanptit.elearing_backend_service.enums.Role.ADMIN)
                .build();

        Authentication fakeAuth =
                new UsernamePasswordAuthenticationToken(fakeUser, null, fakeUser.getAuthorities());

        Mockito.when(authenticationManager.authenticate(any())).thenReturn(fakeAuth);

        AuthenticationService service = new AuthenticationService(authenticationManager, jwtUtil);

        LoginRequest request = new LoginRequest("admin@elearning.com", "admin123");

        // Act
        LoginResponse response = service.login(request);

        // Assert
        assertThat(response.getToken()).isNotEmpty();
        assertThat(response.getUser().getEmail()).isEqualTo("admin@elearning.com");
    }

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

