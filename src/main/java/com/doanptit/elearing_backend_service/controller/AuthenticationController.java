package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ForgotPasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.LoginRequest;
import com.doanptit.elearing_backend_service.dto.req.ResetPasswordRequest;
import com.doanptit.elearing_backend_service.dto.res.LoginResponse;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "2. Authentication", description = "Các API về Đăng nhập, Đăng xuất, Quên mật khẩu")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng và trả về JWT token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công, trả về token"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Sai email hoặc mật khẩu")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa JWT token hiện tại của người dùng (thêm token vào blacklist).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc bị thiếu")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.TOKEN_NOT_PROVIDED);
        }
        String token = authHeader.substring(7);
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công."));
    }

    @Operation(summary = "Quên mật khẩu", description = "Gửi yêu cầu (token) reset mật khẩu về email của người dùng.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gửi email thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy email")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi email reset mật khẩu"));
    }

    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu mới bằng token đã nhận từ email.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reset mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc mật khẩu mới không khớp")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());
        return ResponseEntity.ok(ApiResponse.success("Reset mật khẩu thành công"));
    }
}