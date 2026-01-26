package com.syncme.syncme.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private String userId;
    private String email;
    private String nickname;
    private Long followedAt;
}
