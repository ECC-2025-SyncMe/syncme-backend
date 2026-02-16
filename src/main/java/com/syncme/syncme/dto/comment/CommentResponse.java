package com.syncme.syncme.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String commentId;
    private String authorUserId;
    private String authorNickname;
    private String targetUserId;
    private String content;
    private Long createdAt;
}
