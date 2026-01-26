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
public class Friend {
    
    /**
     * PK = USER#{email}
     * SK = FOLLOWING#{targetEmail} 또는 FOLLOWER#{followerEmail}
     */
    
    private String pk;
    private String sk;
    private String userId;       // 상대방의 userId
    private String email;        // 상대방의 email
    private String nickname;     // 상대방의 nickname
    private Long followedAt;     // 팔로우 시각
    
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
    
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @DynamoDbAttribute("nickname")
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @DynamoDbAttribute("followedAt")
    public Long getFollowedAt() {
        return followedAt;
    }
    
    public void setFollowedAt(Long followedAt) {
        this.followedAt = followedAt;
    }
}
