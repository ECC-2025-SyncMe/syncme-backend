package com.syncme.syncme.controller;

import com.syncme.syncme.dto.admin.TestDataResponse;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 테스트 데이터 생성
     * - 메인 계정 1개 + 친구 계정 3개 (총 4개 계정)
     * - 각 계정마다 과거 N일의 기분 데이터 생성
     * - 메인 계정이 친구 3명 모두 팔로우
     * 
     * @param days 생성할 과거 데이터 일수 (기본값: 14일)
     */
    @PostMapping("/test-data")
    public ResponseEntity<ApiResponse<TestDataResponse>> createTestData(
            @RequestParam(defaultValue = "14") int days
    ) {
        log.info("Creating test data with {} days of history", days);
        
        if (days < 1 || days > 90) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Days must be between 1 and 90"));
        }
        
        TestDataResponse response = adminService.createTestData(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 테스트 데이터 삭제
     * - 모든 테스트 계정 및 관련 데이터 삭제
     */
    @DeleteMapping("/test-data")
    public ResponseEntity<ApiResponse<Void>> deleteTestData() {
        log.info("Deleting all test data");
        adminService.deleteTestData();
        return ResponseEntity.ok(ApiResponse.success("Test data deleted successfully", null));
    }
}
