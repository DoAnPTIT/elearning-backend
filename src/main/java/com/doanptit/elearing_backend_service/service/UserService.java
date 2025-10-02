package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import org.springframework.data.domain.Page;


public interface UserService {
    UserResponseDto createNewUserByAdmin(UserRequestDto request);

    UserResponseDto findUserById(Long id);

    UserResponseDto getUserById(Integer id);

    Page<UserResponseDto> findAllUsers(int pageNo, int pageSize, String... sorts);
}
