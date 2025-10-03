package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.configuration.JwtUtil;
import com.doanptit.elearing_backend_service.dto.req.LoginRequest;
import com.doanptit.elearing_backend_service.dto.res.LoginResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.PasswordResetToken;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.PasswordResetTokenRepository;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            log.debug("Người dùng xác thực thành công: {}", user.getUsername());

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            return new LoginResponse(token, user);

        } catch (AuthenticationException e) {
            log.error("Lỗi xác thực: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void logout(String token) {
        try {
            Date expirationDate = jwtUtil.extractExpiration(token);
            long expirationTimeSeconds = expirationDate.getTime() / 1000;
            jwtBlacklistService.blacklistToken(token, expirationTimeSeconds);
        } catch (JwtException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        } catch (SdkClientException e) {
            throw new AppException(ErrorCode.TOKEN_BLACKLIST_FAILED);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // Forgot password
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + token;
        String subject = "[Elearning PTIT] Yêu cầu đặt lại mật khẩu";
        String body = "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n"
                + "👉 Nhấn vào đây để đặt lại mật khẩu: " + resetLink
                + "\n\nLink này chỉ có hiệu lực trong 15 phút."
                + "\nNếu bạn không yêu cầu, vui lòng bỏ qua email này.";

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    // Reset password
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa token sau khi dùng
        passwordResetTokenRepository.delete(resetToken);
    }

}
