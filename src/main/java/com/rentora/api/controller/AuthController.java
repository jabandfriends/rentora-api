package com.rentora.api.controller;

import com.rentora.api.dto.ApiResponse;
import com.rentora.api.dto.Authentication.*;
import com.rentora.api.exception.BadRequestException;
import com.rentora.api.security.UserPrincipal;
import com.rentora.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest)  {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        LoginResponse loginResponse = authService.login(loginRequest);
        ApiResponse<LoginResponse> response = new ApiResponse<>(true, "Login successful", loginResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfo>> register(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("User registration attempt for email: {}", createUserRequest.getEmail());

        UserInfo userInfo = authService.createUser(createUserRequest);
        ApiResponse<UserInfo> response = new ApiResponse<>(true, "User created successfully", userInfo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) throws BadRequestException {

        log.info("Password change request for user: {}", userPrincipal.getEmail());

        authService.changePassword(userPrincipal.getId(), changePasswordRequest);
        ApiResponse<Object> response = new ApiResponse<>(true, "Password changed successfully", null);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        UserInfo userInfo = authService.getCurrentUser(userPrincipal.getId());
        ApiResponse<UserInfo> response = new ApiResponse<>(true, "User information retrieved successfully", userInfo);

        return ResponseEntity.ok(response);
    }


}
