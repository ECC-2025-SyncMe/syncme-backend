package com.syncme.syncme.entity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    private String email;
    private String googleId;
    private String nickname;
    private Long createdAt;
    private Long updatedAt;
    
    @DynamoDbPartitionKey
    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }
    
    @DynamoDbSecondaryPartitionKey(indexNames = "googleId-index")
    @DynamoDbAttribute("googleId")
    public String getGoogleId() {
        return googleId;
    }
    
    @DynamoDbAttribute("nickname")
    public String getNickname() {
        return nickname;
    }
    
    @DynamoDbAttribute("createdAt")
    public Long getCreatedAt() {
        return createdAt;
    }
    
    @DynamoDbAttribute("updatedAt")
    public Long getUpdatedAt() {
        return updatedAt;
    }
    
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now().toEpochMilli();
        }
        updatedAt = Instant.now().toEpochMilli();
    }
}
