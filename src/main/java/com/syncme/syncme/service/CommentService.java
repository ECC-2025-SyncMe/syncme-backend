package com.syncme.syncme.service;

import com.syncme.syncme.dto.comment.CommentResponse;
import com.syncme.syncme.entity.Comment;
import com.syncme.syncme.entity.User;
import com.syncme.syncme.repository.CommentRepository;
import com.syncme.syncme.repository.FriendRepository;
import com.syncme.syncme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    
    /**
     * 친구에게 댓글 작성 (양쪽 팔로우 필수)
     */
    public CommentResponse createComment(String myEmail, String targetUserId, String content) {
        // 내 정보 조회
        User myUser = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 대상 사용자 조회
        User targetUser = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        // 자기 자신에게 댓글 불가
        if (myUser.getUserId().equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot comment on your own page");
        }
        
        // 양쪽 팔로우 확인 (서로 친구인지) - 주석처리: 누구나 댓글 가능
        // boolean iFollowTarget = friendRepository.isFollowing(myEmail, targetUser.getEmail());
        // boolean targetFollowsMe = friendRepository.isFollowing(targetUser.getEmail(), myEmail);
        // 
        // if (!iFollowTarget || !targetFollowsMe) {
        //     throw new IllegalArgumentException("Can only comment on mutual friends");
        // }
        
        // 댓글 생성
        Comment comment = commentRepository.createComment(
                myEmail,
                myUser.getUserId(),
                myUser.getNickname(),
                targetUserId,
                content
        );
        
        log.info("User {} created comment {} on {}'s page", myEmail, comment.getCommentId(), targetUserId);
        
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .authorUserId(comment.getAuthorUserId())
                .authorNickname(comment.getAuthorNickname())
                .targetUserId(comment.getTargetUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
    
    /**
     * 특정 사용자가 받은 댓글 조회
     */
    public List<CommentResponse> getCommentsByUserId(String targetUserId) {
        // 대상 사용자 존재 확인
        userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        List<Comment> comments = commentRepository.findCommentsByTargetUserId(targetUserId);
        
        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .commentId(comment.getCommentId())
                        .authorUserId(comment.getAuthorUserId())
                        .authorNickname(comment.getAuthorNickname())
                        .targetUserId(comment.getTargetUserId())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 내가 받은 댓글 조회
     */
    public List<CommentResponse> getMyReceivedComments(String myEmail) {
        User myUser = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return getCommentsByUserId(myUser.getUserId());
    }
    
    /**
     * 댓글 삭제 (본인만 가능)
     */
    public void deleteComment(String myEmail, String commentId) {
        User myUser = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 모든 사용자의 댓글에서 해당 commentId 검색
        // (비효율적이지만 DynamoDB 구조상 불가피)
        Comment comment = findCommentById(commentId);
        
        if (comment == null) {
            throw new RuntimeException("Comment not found");
        }
        
        // 본인 댓글인지 확인
        if (!comment.getAuthorEmail().equals(myEmail)) {
            throw new IllegalArgumentException("Can only delete your own comments");
        }
        
        commentRepository.deleteComment(comment.getTargetUserId(), comment.getSk());
        log.info("User {} deleted comment {}", myEmail, commentId);
    }
    
    /**
     * commentId로 댓글 찾기 (전체 스캔)
     */
    private Comment findCommentById(String commentId) {
        // 효율적이지 않지만 삭제 시에만 사용
        // 실제 프로덕션에서는 GSI를 추가하는 것이 좋음
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            List<Comment> comments = commentRepository.findCommentsByTargetUserId(user.getUserId());
            for (Comment comment : comments) {
                if (comment.getCommentId().equals(commentId)) {
                    return comment;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 사용자 삭제 시 관련 댓글 정리
     */
    public void deleteAllCommentsByUser(String email, String userId) {
        // 내가 받은 댓글 삭제
        commentRepository.deleteAllByTargetUserId(userId);
        
        // 내가 작성한 댓글 삭제
        commentRepository.deleteAllByAuthorEmail(email);
    }
}
