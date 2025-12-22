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
import jakarta.transaction.Transactional;
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

            // Cập nhật thời gian đăng nhập
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user); // Lưu vào DB

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
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        passwordResetTokenRepository.softDeleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        String subject = "[Elearning PTIT] Yêu cầu đặt lại mật khẩu";

        // 🔥 SỬA ĐOẠN NÀY: Chuyển từ Text thường sang HTML
        String htmlBody = String.format("""
            <div style="font-family: Arial, sans-serif; padding: 15px; border: 1px solid #ddd; border-radius: 5px;">
                <h3 style="color: #0056b3;">Xin chào %s,</h3>
                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                <div style="margin: 20px 0;">
                    <a href="%s" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        👉 Đặt lại mật khẩu ngay
                    </a>
                </div>
                <p>Hoặc truy cập link sau: <a href="%s">%s</a></p>
                <p style="color: #666; font-size: 13px;">Link này chỉ có hiệu lực trong 15 phút.</p>
                <hr style="border: none; border-top: 1px solid #eee;" />
                <p style="font-size: 12px; color: #999;">Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>
            </div>
            """,
                user.getFirstname() != null ? user.getFirstname() : "bạn", // Lấy tên user cho thân thiện
                resetLink,
                resetLink,
                resetLink
        );

        // Gọi hàm sendEmail (lúc này EmailService mới sẽ gửi HTML đẹp)
        emailService.sendEmail(user.getEmail(), subject, htmlBody);
    }

    // Reset password
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndDeletedOnFalse(token)
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

        // Đánh dấu token đã sử dụng
        resetToken.setDeletedOn(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
