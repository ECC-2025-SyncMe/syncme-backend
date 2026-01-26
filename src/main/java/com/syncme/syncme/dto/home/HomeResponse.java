package com.syncme.syncme.dto.home;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
    private String userId;
    private String nickname;
    private Boolean isFollowing;
    // TODO: Status 담당자가 구현하면 오늘의 상태, 캐릭터 정보 추가
}
