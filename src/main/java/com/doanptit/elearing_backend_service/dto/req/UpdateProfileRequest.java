package com.doanptit.elearing_backend_service.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ không được để trống.")
    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự.")
    private String firstname;

    @NotBlank(message = "Tên không được để trống.")
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự.")
    private String lastname;

    // Tạm thời chỉ là link ảnh, sau này sẽ cập nhật chức năng upload file
    @Size(max = 255, message = "Đường dẫn ảnh quá dài.")
    private String image;
}
