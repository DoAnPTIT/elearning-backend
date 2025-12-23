package com.doanptit.elearing_backend_service.service;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.ChangePasswordRequest;
import com.doanptit.elearing_backend_service.dto.req.UpdateProfileRequest;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.AdminUserListDto;
import com.doanptit.elearing_backend_service.dto.res.BatchCreationResult;
import com.doanptit.elearing_backend_service.dto.res.UploadImageResponse;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    UserResponseDto createNewUserByAdmin(UserRequestDto request);

    UserResponseDto findUserById(Long id);

    UserResponseDto getUserById(Integer id);

    Page<UserResponseDto> findAllUsers(int pageNo, int pageSize, String... sorts);

    Page<AdminUserListDto> findAllUsersForAdmin(int pageNo, int pageSize, String role, String... sorts);

    void changePassword(Integer id, String email, ChangePasswordRequest request);

    UserResponseDto updateProfile(String email, UpdateProfileRequest request);

    BatchCreationResult createUsersFromExcel(MultipartFile file, String role);

    ApiResponse<UploadImageResponse> uploadUserImage(Integer userId, MultipartFile file);

    void completeMySurvey(String email);
}
