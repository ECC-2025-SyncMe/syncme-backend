package com.syncme.syncme.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.home.HomeResponse;
import com.syncme.syncme.dto.user.ShareLinkResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {
    
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    
    @Value("${app.frontend.url:https://syncme-frontend.vercel.app}")
    private String frontendUrl;
    
    public HomeResponse getHome(String userId, String viewerEmail) {
        // 대상 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 팔로우 여부 확인 (로그인한 사용자가 있는 경우)
        Boolean isFollowing = null;
        if (viewerEmail != null) {
            isFollowing = friendRepository.isFollowing(viewerEmail, user.getEmail());
        }
        
        // TODO: Status 담당자가 구현하면 오늘의 상태, 캐릭터 정보 추가
        return HomeResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .isFollowing(isFollowing)
                .build();
    }
    
    public ShareLinkResponse getShareLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String shareLink = frontendUrl + "/home/" + user.getUserId();
        
        return ShareLinkResponse.builder()
                .shareLink(shareLink)
                .userId(user.getUserId())
                .build();
    }
}
