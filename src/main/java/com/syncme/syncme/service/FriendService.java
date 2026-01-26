package com.syncme.syncme.service;

import com.syncme.syncme.dto.friend.FriendResponse;
import com.syncme.syncme.entity.Friend;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {
    
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    
    public void followUser(String myEmail, String targetUserId) {
        // 대상 사용자 조회
        User targetUser = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        // 자기 자신을 팔로우하는지 확인
        User myUser = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (myUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        // 이미 팔로우 중인지 확인
        if (friendRepository.isFollowing(myEmail, targetUser.getEmail())) {
            throw new IllegalArgumentException("Already following this user");
        }
        
        // 팔로우 실행
        friendRepository.follow(myEmail, targetUser.getEmail(), targetUser.getUserId(), targetUser.getNickname());
        log.info("User {} followed {}", myEmail, targetUserId);
    }
    
    public void unfollowUser(String myEmail, String targetUserId) {
        // 대상 사용자 조회
        User targetUser = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        // 팔로우 중인지 확인
        if (!friendRepository.isFollowing(myEmail, targetUser.getEmail())) {
            throw new IllegalArgumentException("Not following this user");
        }
        
        // 언팔로우 실행
        friendRepository.unfollow(myEmail, targetUser.getEmail());
        log.info("User {} unfollowed {}", myEmail, targetUserId);
    }
    
    public List<FriendResponse> getFollowing(String email) {
        List<Friend> friends = friendRepository.findFollowing(email);
        
        return friends.stream()
                .map(friend -> FriendResponse.builder()
                        .userId(friend.getUserId())
                        .email(friend.getEmail())
                        .nickname(friend.getNickname())
                        .followedAt(friend.getFollowedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    public List<FriendResponse> getFollowers(String email) {
        List<Friend> friends = friendRepository.findFollowers(email);
        
        // Follower의 경우 userId와 nickname이 없으므로 User 테이블에서 조회
        return friends.stream()
                .map(friend -> {
                    User user = userRepository.findByEmail(friend.getEmail())
                            .orElse(null);
                    
                    if (user == null) {
                        return null;
                    }
                    
                    return FriendResponse.builder()
                            .userId(user.getUserId())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .followedAt(friend.getFollowedAt())
                            .build();
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    public List<FriendResponse> getFriends(String email) {
        // 서로 팔로우한 사람들 (친구)
        List<Friend> following = friendRepository.findFollowing(email);
        List<Friend> followers = friendRepository.findFollowers(email);
        
        List<String> followingEmails = following.stream()
                .map(Friend::getEmail)
                .collect(Collectors.toList());
        
        List<String> followerEmails = followers.stream()
                .map(Friend::getEmail)
                .collect(Collectors.toList());
        
        // 교집합 구하기 (서로 팔로우)
        List<String> mutualEmails = followingEmails.stream()
                .filter(followerEmails::contains)
                .collect(Collectors.toList());
        
        // 친구 목록 반환
        return following.stream()
                .filter(friend -> mutualEmails.contains(friend.getEmail()))
                .map(friend -> FriendResponse.builder()
                        .userId(friend.getUserId())
                        .email(friend.getEmail())
                        .nickname(friend.getNickname())
                        .followedAt(friend.getFollowedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
