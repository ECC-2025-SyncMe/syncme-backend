package com.syncme.syncme.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    /**
     * PK = COMMENT#{targetUserId}  (댓글을 받은 사람)
     * SK = TIMESTAMP#{timestamp}#{commentId}
     * commentId = c_{UUID}
     */
    
    private String pk;
    private String sk;
    private String commentId;
    private String authorEmail;      // 작성자 이메일
    private String authorUserId;     // 작성자 userId
    private String authorNickname;   // 작성자 닉네임
    private String targetUserId;     // 댓글을 받은 사람의 userId
    private String content;          // 댓글 내용
    private Long createdAt;          // 작성 시간
    
    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }
    
    public void setPk(String pk) {
        this.pk = pk;
    }
    
    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }
    
    public void setSk(String sk) {
        this.sk = sk;
    }
    
    @DynamoDbAttribute("commentId")
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    @DynamoDbAttribute("authorEmail")
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    @DynamoDbAttribute("authorUserId")
    public String getAuthorUserId() {
        return authorUserId;
    }
    
    public void setAuthorUserId(String authorUserId) {
        this.authorUserId = authorUserId;
    }
    
    @DynamoDbAttribute("authorNickname")
    public String getAuthorNickname() {
        return authorNickname;
    }
    
    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }
    
    @DynamoDbAttribute("targetUserId")
    public String getTargetUserId() {
        return targetUserId;
    }
    
    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    @DynamoDbAttribute("content")
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
