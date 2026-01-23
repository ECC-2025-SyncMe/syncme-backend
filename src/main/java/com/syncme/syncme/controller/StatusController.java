package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.status.*;
import com.syncme.syncme.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    // 오늘 상태 조회
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayStatusResponse>> getToday(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TodayStatusResponse response = statusService.getToday(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 오늘 상태 기록 (최초)
    @PostMapping("/today")
    public ResponseEntity<ApiResponse<Void>> createToday(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpsertStatusRequest request
    ) {
        statusService.createToday(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Today's status saved", null));
    }

    // 오늘 상태 수정
    @PutMapping("/today")
    public ResponseEntity<ApiResponse<Void>> updateToday(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpsertStatusRequest request
    ) {
        statusService.updateToday(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Today's status updated", null));
    }

    // 오늘 기록 여부 확인
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<StatusCheckResponse>> checkToday(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StatusCheckResponse response = statusService.checkToday(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 전체 기록 리스트
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoryListResponse>> history(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        HistoryListResponse response = statusService.getHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 날짜 기록 조회
    @GetMapping("/history/{date}")
    public ResponseEntity<ApiResponse<HistoryListResponse.Item>> historyByDate(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String date
    ) {
        HistoryListResponse.Item response = statusService.getHistoryByDate(userDetails.getUsername(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 모든 기록 초기화
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> deleteAllHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        statusService.deleteAllHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("All status history deleted", null));
    }
}
