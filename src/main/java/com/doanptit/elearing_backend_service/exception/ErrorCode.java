package com.doanptit.elearing_backend_service.exception;

import lombok.*;
import org.springframework.http.HttpStatus; // 👈 Cần import

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 404 Not Found
    USER_NOT_FOUND("USER_NOT_FOUND", "User không tồn tại", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND("COURSE_NOT_FOUND", "Khóa học không tồn tại", HttpStatus.NOT_FOUND),

    // 400 Bad Request
    ENROLLMENT_FAILED("ENROLLMENT_FAILED", "Đăng ký khóa học thất bại", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("VALIDATION_ERROR", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),

    // Thêm lỗi đã từng gặp: Email đã tồn tại
    USER_EMAIL_EXISTS("USER_EMAIL_EXISTS", "Email đã được sử dụng", HttpStatus.BAD_REQUEST),

    // 500 Internal Server Error (Lỗi chung)
    INTERNAL_ERROR("INTERNAL_ERROR", "Lỗi hệ thống, vui lòng thử lại sau", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHORIZED("UNATHORIZED", "Bạn không có quyền truy cập", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}