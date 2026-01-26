package com.syncme.syncme.controller;

import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.dto.friend.FriendResponse;
import com.syncme.syncme.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {
    
    private final FriendService friendService;
    
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> followUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId) {
        friendService.followUser(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.success("Successfully followed user", null));
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId) {
        friendService.unfollowUser(userDetails.getUsername(), userId);
        return ResponseEntity.ok(ApiResponse.success("Successfully unfollowed user", null));
    }
    
    @GetMapping("/following")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFollowing(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> following = friendService.getFollowing(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(following));
    }
    
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFollowers(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> followers = friendService.getFollowers(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(followers));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponse> friends = friendService.getFriends(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(friends));
    }
}
