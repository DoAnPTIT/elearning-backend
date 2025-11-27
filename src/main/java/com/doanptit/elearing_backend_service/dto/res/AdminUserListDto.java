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
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserListDto {
    private Integer id;
    private String email;
    private String firstname;
    private String lastname;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;
    
    private Role role;
    private String image;
    private Boolean active;
    
    // Additional fields for admin
    private Integer courseCount;
    private Integer studentCount;
    private Integer enrolledCourses;
    private Integer completedCourses;
    private Double rating;
    private Double progress;
    private String lastActivity;
    private List<String> courseNames;  // List of approved course names for students
}
