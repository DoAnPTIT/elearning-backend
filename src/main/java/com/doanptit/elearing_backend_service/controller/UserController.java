package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.res.UploadImageResponse;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "5. User Actions (Profile)", description = "Các API cho phép người dùng tự quản lý (đổi mật khẩu, profile)")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Đổi mật khẩu",
            description = "Người dùng tự đổi mật khẩu của mình (yêu cầu mật khẩu cũ và mới). Yêu cầu xác thực (USER/ADMIN/TEACHER).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mật khẩu cũ không chính xác hoặc mật khẩu mới không khớp"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền đổi mật khẩu cho user này")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('TEACHER')")
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Parameter(description = "ID của người dùng", required = true) @PathVariable Integer id,
            @RequestBody @Valid ChangePasswordRequest request,
            Principal principal) {
        userService.changePassword(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi mật khẩu thành công"));
    }

    @Operation(summary = "Cập nhật thông tin cá nhân",
            description = "Người dùng tự cập nhật thông tin (tên, ngày sinh...). Yêu cầu xác thực.")
    @PutMapping("/edit-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        UserResponseDto updatedUser = userService.updateProfile(email, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", updatedUser));
    }

    @Operation(summary = "Tải lên ảnh đại diện",
            description = "Tải lên ảnh đại diện (avatar) cho người dùng.")
    @PostMapping("/{userId}/image")
    public ResponseEntity<ApiResponse<UploadImageResponse>> uploadUserImage(
            @Parameter(description = "ID của người dùng", required = true) @PathVariable Integer userId,
            @Parameter(description = "File ảnh", required = true) @RequestParam("file") MultipartFile file
    ) {
        ApiResponse<UploadImageResponse> response = userService.uploadUserImage(userId, file);
        return ResponseEntity.ok(response);
    }
}