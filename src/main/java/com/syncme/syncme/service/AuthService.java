package com.syncme.syncme.service;

import com.syncme.syncme.dto.auth.AuthResponse;
import com.syncme.syncme.dto.auth.GoogleLoginRequest;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.UserRepository;
import com.syncme.syncme.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // TODO: 실제로는 Google ID Token을 검증해야 함
        // 지금은 간단히 idToken을 email로 사용
        String email = extractEmailFromIdToken(request.getIdToken());
        String googleId = extractGoogleIdFromIdToken(request.getIdToken());
        
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createNewUser(email, googleId));
        
        String token = jwtTokenProvider.createToken(user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
    
    private User createNewUser(String email, String googleId) {
        User newUser = User.builder()
                .email(email)
                .googleId(googleId)
                .nickname(generateDefaultNickname(email))
                .build();
        return userRepository.save(newUser);
    }
    
    private String generateDefaultNickname(String email) {
        return "User_" + email.split("@")[0];
    }
    
    // TODO: 실제로는 Google API를 사용하여 ID Token을 검증하고 정보를 추출해야 함
    private String extractEmailFromIdToken(String idToken) {
        // 임시 구현: idToken을 그대로 email로 사용
        return idToken;
    }
    
    private String extractGoogleIdFromIdToken(String idToken) {
        // 임시 구현: idToken의 해시값을 googleId로 사용
        return String.valueOf(idToken.hashCode());
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
