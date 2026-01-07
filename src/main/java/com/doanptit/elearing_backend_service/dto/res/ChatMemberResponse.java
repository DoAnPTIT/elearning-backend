package com.doanptit.elearing_backend_service.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberResponse {
    private Long id;
    private String fullname;
    private String email;
    private String image;
    private Boolean online;
    private String role; // STUDENT / TEACHER / ADMIN
}


