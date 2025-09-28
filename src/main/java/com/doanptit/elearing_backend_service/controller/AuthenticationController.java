package com.doanptit.elearing_backend_service.controller;

import com.doanptit.elearing_backend_service.dto.ApiResponse;
import com.doanptit.elearing_backend_service.dto.req.LoginRequest;
import com.doanptit.elearing_backend_service.dto.res.LoginResponse;
import com.doanptit.elearing_backend_service.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
