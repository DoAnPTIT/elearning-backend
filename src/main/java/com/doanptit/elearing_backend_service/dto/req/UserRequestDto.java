package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.validator.ValidEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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
    @Size(max = 20, message = "Họ không được vượt quá 20 ký tự")
    private String firstname;

    private String lastname;

    @JsonProperty("date_of_birth")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    @NotNull(message = "Vai trò (Role) không được để trống")
    @ValidEnum(enumClass = Role.class, message = "Vai trò không hợp lệ")
    private String role;

    private String image;

    private boolean active = true;

}
