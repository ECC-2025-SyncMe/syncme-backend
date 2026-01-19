package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.settings.SettingsResponse;
import com.syncme.syncme.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final SettingsService settingsService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        SettingsResponse response = settingsService.getSettings(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetData(
            @AuthenticationPrincipal UserDetails userDetails) {
        settingsService.resetData(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Data reset successfully", null));
    }
}
