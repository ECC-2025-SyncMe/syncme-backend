package com.syncme.syncme.controller;

import com.syncme.syncme.dto.auth.AuthResponse;
import com.syncme.syncme.dto.auth.GoogleLoginRequest;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails.getUsername());
        
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
