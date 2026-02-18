package com.syncme.syncme.service;

import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.settings.SettingsResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.CommentRepository;
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
    private final CommentRepository commentRepository;
    
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
        
        // 1. Status History 전체 삭제
        String pk = "USER#" + email;
        statusRepository.deleteAllByPk(pk);
        log.info("All status history deleted for user: {}", email);
        
        // 2. 받은 댓글 삭제
        commentRepository.deleteAllByTargetUserId(user.getUserId());
        log.info("All received comments deleted for user: {}", email);
        
        // 3. 작성한 댓글 삭제
        commentRepository.deleteAllByAuthorEmail(email);
        log.info("All authored comments deleted for user: {}", email);
    }
}
