package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;

public interface UserService {
    UserResponseDto createNewUserByAdmin(UserRequestDto request);

    UserResponseDto findUserById(Long id);
}
