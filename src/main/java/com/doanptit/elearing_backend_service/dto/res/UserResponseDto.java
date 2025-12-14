package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private int id;
    private String email;
    private String firstname;
    private String lastname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    private Role role;
    private String image;
    private Boolean active;
    private String lastActivity;
    
    // Teacher statistics
    private Long courseCount;
    private Long studentCount;
    private Double rating;
    
    // Student statistics
    private Long enrolledCourses;
    private Long completedCourses;
    private Double progress;
    private List<String> courseNames;
    private List<StudentEnrollmentSummaryDto> enrollments;
    
    public UserResponseDto(int id, String email, String firstname, String lastname, 
                          LocalDate dateOfBirth, Role role, String image, Boolean active) {
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
        this.image = image;
        this.active = active;
    }
}
