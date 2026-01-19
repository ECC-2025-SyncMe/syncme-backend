package com.syncme.syncme.service;

import com.syncme.syncme.dto.user.UpdateNicknameRequest;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
    
    @Transactional
    public UserResponse updateNickname(String email, UpdateNicknameRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setNickname(request.getNickname());
        User updatedUser = userRepository.save(user);
        
        return UserResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .build();
    }
    
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userRepository.delete(user);
        log.info("User deleted: {}", email);
    }
}
