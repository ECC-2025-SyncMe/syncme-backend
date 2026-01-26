package com.syncme.syncme.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.user.UpdateNicknameRequest;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.dto.user.UserSearchResponse;
import com.syncme.syncme.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "nickname") String type) {
        List<UserSearchResponse> response = userService.searchUsers(query, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
