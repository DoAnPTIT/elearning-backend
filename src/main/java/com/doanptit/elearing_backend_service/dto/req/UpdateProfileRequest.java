package com.doanptit.elearing_backend_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ không được để trống.")
    @Size(max = 20, message = "Họ không được vượt quá 20 ký tự.")
    private String firstname;

    private String lastname;

    private String image;
}
