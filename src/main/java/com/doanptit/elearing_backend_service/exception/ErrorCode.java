package com.doanptit.elearing_backend_service.exception;

import lombok.*;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User không tồn tại"),
    COURSE_NOT_FOUND("COURSE_NOT_FOUND", "Khóa học không tồn tại"),
    ENROLLMENT_FAILED("ENROLLMENT_FAILED", "Đăng ký khóa học thất bại"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu không hợp lệ"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Lỗi hệ thống, vui lòng thử lại sau");

    private final String code;
    private final String message;
}