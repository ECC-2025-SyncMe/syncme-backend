package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.content.ContentResponse;
import com.syncme.syncme.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class ContentController {
    
    private final ContentService contentService;
    
    @GetMapping("/today-message")
    public ResponseEntity<ApiResponse<ContentResponse>> getTodayMessage() {
        ContentResponse response = contentService.getTodayMessage();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/loading-message")
    public ResponseEntity<ApiResponse<ContentResponse>> getLoadingMessage() {
        ContentResponse response = contentService.getLoadingMessage();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/about")
    public ResponseEntity<ApiResponse<ContentResponse>> getAbout() {
        ContentResponse response = contentService.getAbout();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
