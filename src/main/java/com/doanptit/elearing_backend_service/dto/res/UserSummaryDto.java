package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
    
    private Role role;
    private String image;
    private Boolean active;
    private Boolean hasCompletedSurvey;

    public UserSummaryDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.dateOfBirth = user.getDateOfBirth();
        this.role = user.getRole();
        this.image = user.getImage();
        this.active = user.getActive();
        this.hasCompletedSurvey = user.getHasCompletedSurvey();
    }
}
