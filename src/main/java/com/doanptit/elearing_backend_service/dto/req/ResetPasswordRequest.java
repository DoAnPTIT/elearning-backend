package com.doanptit.elearing_backend_service.dto.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token không được để trống.")
    private String token;

    @JsonProperty("new_password")
    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự.")
    private String newPassword;

    @JsonProperty("confirm_password")
    @NotBlank(message = "Xác nhận mật khẩu không được để trống.")
    private String confirmPassword;
}