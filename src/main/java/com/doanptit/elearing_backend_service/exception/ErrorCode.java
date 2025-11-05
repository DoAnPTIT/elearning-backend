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

    UNAUTHORIZED("UNATHORIZED", "Bạn không có quyền truy cập", HttpStatus.UNAUTHORIZED),

    INVALID_TOKEN("INVALID_TOKEN", "Token không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    TOKEN_BLACKLIST_FAILED("TOKEN_BLACKLIST_FAILED", "Không thể vô hiệu hóa token do lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_NOT_PROVIDED("TOKEN_NOT_PROVIDED", "Token xác thực không được cung cấp", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token đã hết hạn", HttpStatus.UNAUTHORIZED),

    // Lỗi khi đặt lại mật khẩu
    PASSWORD_INCORRECT("PASSWORD_INCORRECT", "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATE("PASSWORD_DUPLICATE", "Mật khẩu mới không được trùng với mật khẩu cũ", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH( "PASSWORD_NOT_MATCH", "Mật khẩu mới và xác nhận không khớp", HttpStatus.BAD_REQUEST),

    // File upload
    FILE_IS_EMPTY("FILE_IS_EMPTY", "File không được để trống", HttpStatus.BAD_REQUEST),
    FILE_PROCESSING_ERROR("FILE_PROCESSING_ERROR", "Tạo file lỗi",  HttpStatus.BAD_REQUEST ),
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "Tải ảnh thất bại", HttpStatus.BAD_REQUEST),
    FILE_INVALID_TYPE("FILE_INVALID_TYPE", "Định dạng ảnh không hợp lệ (chỉ hỗ trợ JPEG, PNG)", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_TOO_LARGE", "Ảnh vượt quá dung lượng cho phép (2MB)", HttpStatus.BAD_REQUEST),

    INVALID_ROLE("INVALID_ROLE", "Role không hợp lệ", HttpStatus.BAD_REQUEST),

    SECTION_NOT_FOUND("SECTION_NOT_FOUND", "Khong tim thay section", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND("LESSON_NOT_FOUND", "Khong tim thay section", HttpStatus.NOT_FOUND),

    // Submit course
    LESSON_NOT_VIDEO_TYPE("LESSON_NOT_VIDEO_TYPE", "Loại bài giảng không phải là video", HttpStatus.BAD_REQUEST),
    INVALID_COURSE_STATUS_FOR_REVIEW("INVALID_COURSE_STATUS_FOR_REVIEW", "Trạng thái khóa học không hợp lệ để gửi duyệt", HttpStatus.BAD_REQUEST),
    COURSE_MISSING_COVER_IMAGE("COURSE_MISSING_COVER_IMAGE", "Khóa học phải có ảnh bìa để gửi duyệt", HttpStatus.BAD_REQUEST),
    COURSE_IS_EMPTY("COURSE_IS_EMPTY", "Khóa học phải có ít nhất một bài giảng hoặc bài kiểm tra", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}