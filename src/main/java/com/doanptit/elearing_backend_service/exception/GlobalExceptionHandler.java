package com.doanptit.elearing_backend_service.exception;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        HttpStatus httpStatus = ex.getErrorCode().getHttpStatus();
        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponse.error(
                        httpStatus.value(),
                        ex.getErrorCode().getMessage(),
                        ex.getErrorCode().getCode()
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Email hoặc mật khẩu không chính xác.",
                        "AUTH_INVALID_CREDENTIALS"
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN) // 403
                .body(ApiResponse.error(
                        HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền truy cập tài nguyên này.",
                        "AUTH_ACCESS_DENIED"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        String field = ex.getBindingResult().getFieldError().getField();

        String detailedMessage = field + ": " + message;

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        detailedMessage,
                        ErrorCode.VALIDATION_ERROR.getCode()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleEnumParseError(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(400, "Giá trị enum không hợp lệ", "ENUM_INVALID")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Lỗi hệ thống nội bộ.",
                        ErrorCode.INTERNAL_ERROR.getCode()
                ));
    }
}