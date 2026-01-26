package com.syncme.syncme.service;

import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.settings.SettingsResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
    
    private final UserRepository userRepository;
    
    public SettingsResponse getSettings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return SettingsResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .version("1.0.0")
                .build();
    }
    
    public void resetData(String email) {
        // TODO: 실제로는 사용자의 모든 기록 데이터를 삭제해야 함
        // 현재는 User는 유지하고 로그만 남김
        log.info("Data reset requested for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 닉네임을 기본값으로 재설정
        user.setNickname("User_" + email.split("@")[0]);
        userRepository.save(user);
    }
}
