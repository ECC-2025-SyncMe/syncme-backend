package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.home.HomeResponse;
import com.syncme.syncme.dto.user.ShareLinkResponse;
import com.syncme.syncme.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {
    
    private final HomeService homeService;
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<HomeResponse>> getHome(
            @PathVariable String userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 로그인하지 않은 사용자도 볼 수 있도록 처리
        String viewerEmail = userDetails != null ? userDetails.getUsername() : null;
        
        HomeResponse response = homeService.getHome(userId, viewerEmail);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/me/share-link")
    public ResponseEntity<ApiResponse<ShareLinkResponse>> getShareLink(
            @AuthenticationPrincipal UserDetails userDetails) {
        ShareLinkResponse response = homeService.getShareLink(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
