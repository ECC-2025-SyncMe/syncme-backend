package com.syncme.syncme.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.syncme.syncme.dto.comment.CommentResponse;
import com.syncme.syncme.dto.home.HomeResponse;
import com.syncme.syncme.dto.status.HistoryListResponse;
import com.syncme.syncme.dto.status.TodayStatusResponse;
import com.syncme.syncme.dto.user.ShareLinkResponse;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {
    
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final StatusService statusService;
    private final CommentService commentService;
    
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
        
        // 오늘의 상태 조회
        TodayStatusResponse todayStatus = statusService.getToday(user.getEmail());
        
        // 상태 히스토리 조회
        HistoryListResponse statusHistory = statusService.getHistory(user.getEmail());
        
        // 받은 댓글 조회
        List<CommentResponse> receivedComments = commentService.getCommentsByUserId(userId);
        
        return HomeResponse.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .isFollowing(isFollowing)
                .todayStatus(todayStatus)
                .statusHistory(statusHistory)
                .receivedComments(receivedComments)
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
