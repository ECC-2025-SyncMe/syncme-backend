package com.syncme.syncme.controller;

import com.syncme.syncme.dto.calculate.CalculatePreviewResponse;
import com.syncme.syncme.dto.calculate.CalculateStatusResponse;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.status.UpsertStatusRequest;
import com.syncme.syncme.service.CalculateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calculate")
@RequiredArgsConstructor
public class CalculateController {

    private final CalculateService calculateService;

    // 상태 입력 → 계산 결과 반환 (저장 X)
    @PostMapping("/status")
    public ResponseEntity<ApiResponse<CalculateStatusResponse>> calculate(
            @Valid @RequestBody UpsertStatusRequest request
    ) {
        CalculateStatusResponse response = calculateService.calculate(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 저장된 오늘 상태 기반 프리뷰
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<CalculatePreviewResponse>> preview(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CalculatePreviewResponse response = calculateService.preview(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
