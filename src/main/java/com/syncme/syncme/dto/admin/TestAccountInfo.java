package com.syncme.syncme.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAccountInfo {
    private String userId;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;
}
