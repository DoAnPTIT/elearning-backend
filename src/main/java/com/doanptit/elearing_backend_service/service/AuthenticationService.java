package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.configuration.JwtUtil;
import com.doanptit.elearing_backend_service.dto.req.LoginRequest;
import com.doanptit.elearing_backend_service.dto.res.LoginResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.model.User;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.util.Date;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

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
}
