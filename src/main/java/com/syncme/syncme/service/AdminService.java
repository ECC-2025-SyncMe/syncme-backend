package com.syncme.syncme.service;

import com.syncme.syncme.dto.admin.TestAccountInfo;
import com.syncme.syncme.dto.admin.TestDataResponse;
import com.syncme.syncme.entity.DailyStatus;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.StatusRepository;
import com.syncme.syncme.repository.UserRepository;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final FriendRepository friendRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Random random = new Random();
    
    /**
     * 테스트용 계정 4개 생성 + 과거 14일 데이터 + 친구 관계 설정
     */
    public TestDataResponse createTestData(int daysOfData) {
        log.info("Creating test data with {} days of history", daysOfData);
        
        // 1. 메인 테스트 계정 생성
        TestAccountInfo mainAccount = createTestAccount("testmain@syncme.com", "TestMain User");
        
        // 2. 친구 계정 3개 생성
        List<TestAccountInfo> friendAccounts = new ArrayList<>();
        friendAccounts.add(createTestAccount("testfriend1@syncme.com", "TestFriend 1"));
        friendAccounts.add(createTestAccount("testfriend2@syncme.com", "TestFriend 2"));
        friendAccounts.add(createTestAccount("testfriend3@syncme.com", "TestFriend 3"));
        
        // 3. 모든 계정에 과거 데이터 생성
        createHistoricalData(mainAccount.getEmail(), daysOfData);
        for (TestAccountInfo friend : friendAccounts) {
            createHistoricalData(friend.getEmail(), daysOfData);
        }
        
        // 4. 메인 계정이 친구 3명을 모두 팔로우
        for (TestAccountInfo friend : friendAccounts) {
            try {
                friendRepository.follow(
                    mainAccount.getEmail(), 
                    friend.getEmail(), 
                    friend.getUserId(), 
                    friend.getNickname()
                );
                log.info("Main account followed: {}", friend.getEmail());
            } catch (Exception e) {
                log.warn("Failed to create friend relationship: {}", e.getMessage());
            }
        }
        
        return TestDataResponse.builder()
                .mainAccount(mainAccount)
                .friendAccounts(friendAccounts)
                .daysOfData(daysOfData)
                .message("Test data created successfully! Total 4 accounts with " + daysOfData + " days of mood data each.")
                .build();
    }
    
    /**
     * 테스트 계정 생성 (User + JWT 토큰)
     */
    private TestAccountInfo createTestAccount(String email, String nickname) {
        // 기존 계정이 있으면 삭제
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            userRepository.delete(existingUser);
            log.info("Deleted existing test user: {}", email);
        });
        
        // 새 계정 생성
        String userId = generateUserId();
        String googleId = "test_google_" + userId;
        
        User user = User.builder()
                .email(email)
                .userId(userId)
                .googleId(googleId)
                .nickname(nickname)
                .build();
        
        userRepository.save(user);
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        
        log.info("Created test account: {} (userId: {})", email, userId);
        
        return TestAccountInfo.builder()
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    
    /**
     * 과거 N일간의 DailyStatus 데이터 생성
     */
    private void createHistoricalData(String email, int days) {
        LocalDate today = ZonedDateTime.now(KST).toLocalDate();
        String pk = "USER#" + email;
        
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.toString(); // YYYY-MM-DD
            String sk = "DATE#" + dateStr;
            
            // 랜덤한 기분 데이터 생성 (다양한 패턴)
            int energy = generateMoodValue(i, days);
            int burden = generateMoodValue(i, days);
            int passion = generateMoodValue(i, days);
            
            DailyStatus status = new DailyStatus();
            status.setPk(pk);
            status.setSk(sk);
            status.setEnergy(energy);
            status.setBurden(burden);
            status.setPassion(passion);
            status.setCreatedAt(date.atStartOfDay(KST).toString());
            status.setUpdatedAt(date.atStartOfDay(KST).toString());
            
            statusRepository.put(status);
        }
        
        log.info("Created {} days of historical data for {}", days, email);
    }
    
    /**
     * 다양한 기분 데이터 생성 (ACTIVE/TIRED 상태가 더 많이 나오도록 조정)
     */
    private int generateMoodValue(int dayOffset, int totalDays) {
        // 3가지 패턴으로 ACTIVE/TIRED 상태가 더 많이 나오도록 조정
        int pattern = random.nextInt(3);
        int value;
        
        switch (pattern) {
            case 0: // 낮은 점수대 (10~40) - TIRED 유도
                value = 10 + random.nextInt(31);
                break;
            case 1: // 중간 점수대 (40~65) - NORMAL 유도
                value = 40 + random.nextInt(26);
                break;
            case 2: // 높은 점수대 (65~95) - ACTIVE 유도
                value = 65 + random.nextInt(31);
                break;
            default:
                value = 20 + random.nextInt(71);
        }
        
        return value;
    }
    
    /**
     * userId 생성 (u_로 시작하는 12자리)
     */
    private String generateUserId() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        return "u_" + uuid.substring(0, 12);
    }
    
    /**
     * 테스트 데이터 전체 삭제
     */
    public void deleteTestData() {
        List<String> testEmails = List.of(
            "testmain@syncme.com",
            "testfriend1@syncme.com",
            "testfriend2@syncme.com",
            "testfriend3@syncme.com"
        );
        
        for (String email : testEmails) {
            // User 삭제
            userRepository.findByEmail(email).ifPresent(user -> {
                userRepository.delete(user);
                log.info("Deleted test user: {}", email);
            });
            
            // DailyStatus 삭제
            String pk = "USER#" + email;
            List<DailyStatus> statuses = statusRepository.findAllByPkOrderBySkDesc(pk);
            for (DailyStatus status : statuses) {
                statusRepository.deleteByPkSk(status.getPk(), status.getSk());
            }
            log.info("Deleted {} status records for {}", statuses.size(), email);
            
            // Friend 관계 삭제는 자동으로 처리됨 (cascade)
        }
        
        log.info("All test data deleted successfully");
    }
}
