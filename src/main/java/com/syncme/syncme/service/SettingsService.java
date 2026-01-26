package com.syncme.syncme.service;

import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.settings.SettingsResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.StatusRepository;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
    
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    
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
    
    public void deleteData(String email) {
        log.info("Data deletion requested for user: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Status History 전체 삭제
        String pk = "USER#" + email;
        statusRepository.deleteAllByPk(pk);
        
        log.info("All status history deleted for user: {}", user.getEmail());
    }
}
