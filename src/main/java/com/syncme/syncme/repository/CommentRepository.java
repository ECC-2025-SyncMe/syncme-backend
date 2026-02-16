package com.syncme.syncme.repository;

import com.syncme.syncme.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table.comments:syncme-comments}")
    private String tableName;

    private DynamoDbTable<Comment> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Comment.class));
    }

    public Comment createComment(String authorEmail, String authorUserId, String authorNickname, 
                                  String targetUserId, String content) {
        Long now = Instant.now().toEpochMilli();
        String commentId = "c_" + UUID.randomUUID().toString().replace("-", "");
        
        Comment comment = Comment.builder()
                .pk("COMMENT#" + targetUserId)
                .sk("TIMESTAMP#" + now + "#" + commentId)
                .commentId(commentId)
                .authorEmail(authorEmail)
                .authorUserId(authorUserId)
                .authorNickname(authorNickname)
                .targetUserId(targetUserId)
                .content(content)
                .createdAt(now)
                .build();
        
        table().putItem(comment);
        return comment;
    }

    public List<Comment> findCommentsByTargetUserId(String targetUserId) {
        List<Comment> result = new ArrayList<>();
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue("COMMENT#" + targetUserId)
                                .sortValue("TIMESTAMP#")
                                .build()
                ))
                .scanIndexForward(false)  // 최신순 정렬
                .build();
        
        for (Page<Comment> page : table().query(request)) {
            result.addAll(page.items());
        }
        
        return result;
    }

    public Optional<Comment> findByCommentId(String commentId, String targetUserId) {
        List<Comment> comments = findCommentsByTargetUserId(targetUserId);
        return comments.stream()
                .filter(c -> c.getCommentId().equals(commentId))
                .findFirst();
    }

    public void deleteComment(String targetUserId, String sk) {
        table().deleteItem(Key.builder()
                .partitionValue("COMMENT#" + targetUserId)
                .sortValue(sk)
                .build());
    }

    public void deleteAllByTargetUserId(String targetUserId) {
        List<Comment> comments = findCommentsByTargetUserId(targetUserId);
        for (Comment comment : comments) {
            table().deleteItem(Key.builder()
                    .partitionValue(comment.getPk())
                    .sortValue(comment.getSk())
                    .build());
        }
    }

    public void deleteAllByAuthorEmail(String authorEmail) {
        // 모든 사용자의 댓글을 스캔해서 작성자가 authorEmail인 것을 삭제
        // (효율적이지 않지만 계정 삭제 시에만 사용)
        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder().build();
        
        for (Page<Comment> page : table().scan(scanRequest)) {
            for (Comment comment : page.items()) {
                if (comment.getAuthorEmail().equals(authorEmail)) {
                    table().deleteItem(Key.builder()
                            .partitionValue(comment.getPk())
                            .sortValue(comment.getSk())
                            .build());
                }
            }
        }
    }
}
