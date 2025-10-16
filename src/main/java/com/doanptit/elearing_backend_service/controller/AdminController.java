package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.UserRequestDto;
import com.doanptit.elearing_backend_service.dto.res.AdminStatisticsDto;
import com.doanptit.elearing_backend_service.dto.res.BatchCreationResult;
import com.doanptit.elearing_backend_service.dto.res.UserResponseDto;
import com.doanptit.elearing_backend_service.service.AdminStatisticsService;
import com.doanptit.elearing_backend_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminStatisticsService adminStatisticsService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@RequestBody @Valid UserRequestDto request) {
        UserResponseDto newUserDto = userService.createNewUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newUserDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Integer id) {
        UserResponseDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "id,asc") String[] sort) {
        return ResponseEntity.ok(ApiResponse.success(userService.findAllUsers(page, size, sort)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AdminStatisticsDto>> getStatisticsQuantityByRole() {
        AdminStatisticsDto stats = adminStatisticsService.getStatisticsQuantityByRole();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/batch-create")
    public ResponseEntity<ApiResponse<BatchCreationResult>> createUsersFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("role") String role) {

        var result = userService.createUsersFromExcel(file, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }

}
