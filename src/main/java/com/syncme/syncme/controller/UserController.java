package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.user.UpdateNicknameRequest;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUserInfo(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest request) {
        UserResponse response = userService.updateNickname(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Nickname updated successfully", response));
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
