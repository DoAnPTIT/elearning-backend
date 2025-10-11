package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('TEACHER')")
    @PatchMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@PathVariable Integer id,
                                                              @RequestBody @Valid ChangePasswordRequest request,
                                                              Principal principal) {
        userService.changePassword(id, principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã thay đổi mật khẩu thành công"));
    }

    @PutMapping("/edit-profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();
        UserResponseDto updatedUser = userService.updateProfile(email, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", updatedUser));
    }

}
