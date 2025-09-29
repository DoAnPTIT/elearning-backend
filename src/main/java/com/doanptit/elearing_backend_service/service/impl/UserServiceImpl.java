package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.UserMapper;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto createNewUserByAdmin(UserRequestDto request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new AppException(ErrorCode.USER_EMAIL_EXISTS);
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .role(request.getRole())
                .active(request.isActive())
                .build();

        User savedUser = userRepository.save(newUser);

        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public UserResponseDto findUserById(Long id) {
        return null;
    }

    @Override
    public UserResponseDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getCurrentUserInfo() {
        String email = Optional.ofNullable(
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        ).orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponseDto(user);
    }
}
