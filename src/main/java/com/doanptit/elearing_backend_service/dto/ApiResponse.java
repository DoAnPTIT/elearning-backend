package com.doanptit.elearing_backend_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ApiResponse<T> {
    private int status;
    private String message;
    private String errorCode;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int status, String message, String errorCode, T data) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", null, data);
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode) {
        return new ApiResponse<>(status, message, errorCode, null);
    }
}
