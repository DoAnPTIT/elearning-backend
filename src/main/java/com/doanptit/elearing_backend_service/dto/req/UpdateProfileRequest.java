package com.doanptit.elearing_backend_service.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Họ không được để trống.")
    @Size(max = 20, message = "Họ không được vượt quá 20 ký tự.")
    private String firstname;

    private String lastname;

    private String image;

    @JsonProperty("date_of_birth")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
}
