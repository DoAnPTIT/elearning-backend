package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.validator.ValidEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Họ không được để trống")
    @Size(max = 20, message = "Họ không được vượt quá 50 ký tự")
    private String firstname;

    private String lastname;

    @NotNull(message = "Vai trò (Role) không được để trống")
    @ValidEnum(enumClass = Role.class, message = "Vai trò không hợp lệ")
    private String role;

    private String image;

    private boolean active = true;

}
