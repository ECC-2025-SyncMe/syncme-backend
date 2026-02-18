package com.syncme.syncme.repository;

import com.syncme.syncme.entity.Friend;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table.friends:syncme-friends}")
    private String tableName;

    private DynamoDbTable<Friend> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(Friend.class));
    }

    public void follow(String myEmail, String targetEmail, String targetUserId, String targetNickname) {
        Long now = Instant.now().toEpochMilli();
        
        // 내가 팔로우한 사람 기록
        Friend following = Friend.builder()
                .pk("USER#" + myEmail)
                .sk("FOLLOWING#" + targetEmail)
                .userId(targetUserId)
                .email(targetEmail)
                .nickname(targetNickname)
                .followedAt(now)
                .build();
        table().putItem(following);
        
        // 상대방의 팔로워 기록
        Friend follower = Friend.builder()
                .pk("USER#" + targetEmail)
                .sk("FOLLOWER#" + myEmail)
                .userId(null)  // 내 userId는 나중에 조회 시 추가
                .email(myEmail)
                .nickname(null)  // 내 nickname도 나중에 조회 시 추가
                .followedAt(now)
                .build();
        table().putItem(follower);
    }

    public void unfollow(String myEmail, String targetEmail) {
        // 내가 팔로우한 기록 삭제
        table().deleteItem(Key.builder()
                .partitionValue("USER#" + myEmail)
                .sortValue("FOLLOWING#" + targetEmail)
                .build());
        
        // 상대방의 팔로워 기록 삭제
        table().deleteItem(Key.builder()
                .partitionValue("USER#" + targetEmail)
                .sortValue("FOLLOWER#" + myEmail)
                .build());
    }

    public List<Friend> findFollowing(String email) {
        List<Friend> result = new ArrayList<>();
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue("USER#" + email)
                                .sortValue("FOLLOWING#")
                                .build()
                ))
                .build();
        
        for (Page<Friend> page : table().query(request)) {
            result.addAll(page.items());
        }
        
        return result;
    }

    public List<Friend> findFollowers(String email) {
        List<Friend> result = new ArrayList<>();
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder()
                                .partitionValue("USER#" + email)
                                .sortValue("FOLLOWER#")
                                .build()
                ))
                .build();
        
        for (Page<Friend> page : table().query(request)) {
            result.addAll(page.items());
        }
        
        return result;
    }

    public boolean isFollowing(String myEmail, String targetEmail) {
        Key key = Key.builder()
                .partitionValue("USER#" + myEmail)
                .sortValue("FOLLOWING#" + targetEmail)
                .build();
        
        return table().getItem(key) != null;
    }

    public void deleteAllByEmail(String email) {
        // 1. 내가 팔로우한 사람들 목록 조회
        List<Friend> following = findFollowing(email);
        for (Friend friend : following) {
            // 내 팔로잉 레코드 삭제: USER#myEmail / FOLLOWING#targetEmail
            table().deleteItem(Key.builder()
                    .partitionValue(friend.getPk())
                    .sortValue(friend.getSk())
                    .build());
            
            // 상대방의 팔로워 레코드도 삭제: USER#targetEmail / FOLLOWER#myEmail
            String targetEmail = friend.getEmail();
            table().deleteItem(Key.builder()
                    .partitionValue("USER#" + targetEmail)
                    .sortValue("FOLLOWER#" + email)
                    .build());
        }
        
        // 2. 나를 팔로우한 사람들 목록 조회
        List<Friend> followers = findFollowers(email);
        for (Friend friend : followers) {
            // 내 팔로워 레코드 삭제: USER#myEmail / FOLLOWER#followerEmail
            table().deleteItem(Key.builder()
                    .partitionValue(friend.getPk())
                    .sortValue(friend.getSk())
                    .build());
            
            // SK에서 팔로워 이메일 추출: FOLLOWER#followerEmail -> followerEmail
            String followerEmail = friend.getSk().replace("FOLLOWER#", "");
            
            // 상대방의 팔로잉 레코드도 삭제: USER#followerEmail / FOLLOWING#myEmail
            table().deleteItem(Key.builder()
                    .partitionValue("USER#" + followerEmail)
                    .sortValue("FOLLOWING#" + email)
                    .build());
        }
    }
}
