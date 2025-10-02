package com.doanptit.elearing_backend_service.service.impl;

import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.enums.Role;
import com.doanptit.elearing_backend_service.exception.AppException;
import com.doanptit.elearing_backend_service.exception.ErrorCode;
import com.doanptit.elearing_backend_service.mapper.UserMapper;
import com.doanptit.elearing_backend_service.model.User;
import com.doanptit.elearing_backend_service.repository.UserRepository;
import com.doanptit.elearing_backend_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
                .role(Role.valueOf(request.getRole()))
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
    public Page<UserResponseDto> findAllUsers(int pageNo, int pageSize, String... sorts) {
        List<Sort.Order> orders = new ArrayList<>();
        if(sorts!=null){
            for(String sortBy: sorts){
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if(matcher.find()){
                    if(matcher.group(3).equalsIgnoreCase("asc")){
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    }else{
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }
        Pageable pageable = PageRequest.of(pageNo,pageSize,Sort.by(orders));
        return this.userRepository.findAll(pageable).map(userMapper::toUserResponseDto);
    }

        @Override
    public void changePassword(Integer id, String email, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Đảm bảo user chỉ đổi mật khẩu của chính mình
        if (!user.getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_DUPLICATE);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }



}
