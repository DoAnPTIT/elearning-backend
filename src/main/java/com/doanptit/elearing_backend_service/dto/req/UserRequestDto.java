package com.doanptit.elearing_backend_service.dto.req;

import com.doanptit.elearing_backend_service.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private Role role;   // ADMIN, TEACHER, STUDENT
    private String image;

}
