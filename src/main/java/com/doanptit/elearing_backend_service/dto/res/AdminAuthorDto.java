package com.doanptit.elearing_backend_service.dto.res;

import lombok.Data;

@Data
public class AdminAuthorDto {
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
}