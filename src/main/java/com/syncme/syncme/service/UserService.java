package com.syncme.syncme.service;

import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.user.UpdateNicknameRequest;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
    
    public UserResponse updateNickname(String email, UpdateNicknameRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setNickname(request.getNickname());
        User updatedUser = userRepository.save(user);
        
        return UserResponse.builder()
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .build();
    }
    
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        userRepository.delete(user);
        log.info("User deleted: {}", email);
    }
}
