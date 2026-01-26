package com.syncme.syncme.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.user.UpdateNicknameRequest;
import com.syncme.syncme.dto.user.UserResponse;
import com.syncme.syncme.dto.user.UserSearchResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.repository.StatusRepository;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final StatusRepository statusRepository;
    
    public UserResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.builder()
                .userId(user.getUserId())
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
                .userId(updatedUser.getUserId())
                .email(updatedUser.getEmail())
                .nickname(updatedUser.getNickname())
                .build();
    }
    
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 1. 사용자의 모든 Friend 관계 삭제
        friendRepository.deleteAllByEmail(email);
        log.info("All friend relationships deleted for user: {}", email);
        
        // 2. 사용자의 모든 DailyStatus 데이터 삭제
        String pk = "USER#" + email;
        statusRepository.deleteAllByPk(pk);
        log.info("All daily status data deleted for user: {}", email);
        
        // 3. User 계정 삭제
        userRepository.delete(user);
        log.info("User account deleted: {}", email);
    }
    
    public List<UserSearchResponse> searchUsers(String query, String searchType) {
        List<User> users;
        
        if ("email".equalsIgnoreCase(searchType)) {
            users = userRepository.searchByEmail(query);
        } else {
            // 기본값은 닉네임 검색
            users = userRepository.searchByNickname(query);
        }
        
        return users.stream()
                .map(user -> UserSearchResponse.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .build())
                .collect(Collectors.toList());
    }
}
