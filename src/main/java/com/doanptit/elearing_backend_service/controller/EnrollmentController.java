package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Chỉ STUDENT mới được đăng ký
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> enrollCourse(
            @PathVariable Long courseId,
            Authentication authentication) {

        enrollmentService.enrollCourse(courseId, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công. Vui lòng chờ Teacher phê duyệt."));
    }
}