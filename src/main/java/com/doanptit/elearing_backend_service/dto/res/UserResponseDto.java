package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserResponseDto {
    private int id;
    private String email;
    private String firstname;
    private String lastname;
    private LocalDate dateOfBirth;
    private Role role;
    private String image;
    private Boolean active;
}
