package com.syncme.syncme.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.syncme.syncme.entity.User;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    
    private final DynamoDbEnhancedClient enhancedClient;
    
    private DynamoDbTable<User> getUserTable() {
        return enhancedClient.table("syncme-users", TableSchema.fromBean(User.class));
    }
    
    public Optional<User> findByEmail(String email) {
        Key key = Key.builder()
                .partitionValue(email)
                .build();
        
        User user = getUserTable().getItem(key);
        return Optional.ofNullable(user);
    }
    
    public Optional<User> findByGoogleId(String googleId) {
        DynamoDbIndex<User> index = getUserTable().index("googleId-index");
        
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(googleId).build());
        
        return index.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
    
    public Optional<User> findByUserId(String userId) {
        DynamoDbIndex<User> index = getUserTable().index("userId-index");
        
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());
        
        return index.query(queryConditional)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
    
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }
    
    public User save(User user) {
        user.prePersist();
        getUserTable().putItem(user);
        return user;
    }
    
    public void delete(User user) {
        Key key = Key.builder()
                .partitionValue(user.getEmail())
                .build();
        getUserTable().deleteItem(key);
    }
    
    public List<User> searchByNickname(String query) {
        String lowerQuery = query.toLowerCase();
        
        return getUserTable().scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .filter(user -> user.getNickname().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
    
    public List<User> searchByEmail(String query) {
        String lowerQuery = query.toLowerCase();
        
        return getUserTable().scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .filter(user -> user.getEmail().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}
