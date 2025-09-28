package com.doanptit.elearing_backend_service.dto.res;

import com.doanptit.elearing_backend_service.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private UserSummaryDto user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserSummaryDto(user);
    }
}
