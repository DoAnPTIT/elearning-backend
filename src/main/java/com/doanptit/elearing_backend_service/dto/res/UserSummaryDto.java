package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.model.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummaryDto {
    private String email;
    private String firstname;
    private String lastname;
    private Role role;

    public UserSummaryDto(User user) {
        this.email = user.getEmail();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.role = user.getRole();
    }
}
