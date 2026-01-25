package com.syncme.syncme.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.syncme.syncme.dto.auth.AuthResponse;
import com.syncme.syncme.dto.auth.GoogleLoginRequest;
import com.syncme.syncme.dto.auth.RefreshTokenRequest;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/google/login")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // 클라이언트에서 JWT 토큰을 삭제하도록 함
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        UserResponse response = UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
