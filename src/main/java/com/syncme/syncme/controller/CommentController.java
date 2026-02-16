package com.syncme.syncme.controller;

import com.syncme.syncme.dto.comment.CommentResponse;
import com.syncme.syncme.dto.comment.CreateCommentRequest;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 친구에게 댓글 작성 (양쪽 팔로우 필수)
     * POST /friends/{userId}/comments
     */
    @PostMapping("/friends/{userId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String userId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(
                userDetails.getUsername(),
                userId,
                request.getContent()
        );
        return ResponseEntity.ok(ApiResponse.success("Comment created successfully", response));
    }
    
    /**
     * 친구가 받은 댓글 조회
     * GET /friends/{userId}/comments
     */
    @GetMapping("/friends/{userId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByUserId(
            @PathVariable String userId) {
        List<CommentResponse> comments = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
    
    /**
     * 내가 받은 댓글 조회
     * GET /comments/received
     */
    @GetMapping("/comments/received")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getMyReceivedComments(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CommentResponse> comments = commentService.getMyReceivedComments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
    
    /**
     * 내가 작성한 댓글 삭제
     * DELETE /comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String commentId) {
        commentService.deleteComment(userDetails.getUsername(), commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
