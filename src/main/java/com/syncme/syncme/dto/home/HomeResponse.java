package com.syncme.syncme.dto.home;

import com.syncme.syncme.dto.comment.CommentResponse;
import com.syncme.syncme.dto.status.HistoryListResponse;
import com.syncme.syncme.dto.status.TodayStatusResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
    private String userId;
    private String nickname;
    private Boolean isFollowing;
    private TodayStatusResponse todayStatus;
    private HistoryListResponse statusHistory;
    private List<CommentResponse> receivedComments;
}
