package com.syncme.syncme.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.syncme.syncme.dto.auth.AuthResponse;
import com.syncme.syncme.dto.auth.GoogleLoginRequest;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.UserRepository;
import com.syncme.syncme.util.GoogleTokenVerifier;
import com.syncme.syncme.util.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            // Google ID Token 검증
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());
            
            // 사용자 정보 추출
            String email = payload.getEmail();
            String googleId = payload.getSubject();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            
            // 이메일 검증 여부 확인
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                throw new IllegalArgumentException("Email not verified by Google");
            }
            
            // 사용자 조회 또는 생성
            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> createNewUser(email, googleId, name, picture));
            
            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(user.getEmail());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
            
            return AuthResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .build();
                    
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to verify Google ID token", e);
            throw new RuntimeException("Invalid Google token", e);
        }
    }
    
    private User createNewUser(String email, String googleId, String name, String picture) {
        String userId = generateUserId();
        
        User newUser = User.builder()
                .email(email)
                .userId(userId)
                .googleId(googleId)
                .nickname(name != null ? name : generateDefaultNickname(email))
                .build();
        return userRepository.save(newUser);
    }
    
    private String generateUserId() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        return "u_" + uuid.substring(0, 12);
    }
    
    private String generateDefaultNickname(String email) {
        return "User_" + email.split("@")[0];
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Not a refresh token");
        }
        
        // 사용자 정보 추출
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = getCurrentUser(email);
        
        // 새로운 Access Token 발급
        String newToken = jwtTokenProvider.createToken(user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
        
        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
