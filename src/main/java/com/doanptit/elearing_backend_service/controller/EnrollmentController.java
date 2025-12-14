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

    // Lấy enrollment info của student cho 1 course cụ thể
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<?>> getEnrollmentForCourse(
            @PathVariable Long courseId,
            Authentication authentication) {

        var enrollment = enrollmentService.getEnrollmentForCourse(courseId, authentication.getName());
        
        if (enrollment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Chưa đăng ký khóa học này", "NOT_ENROLLED"));
        }

        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

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