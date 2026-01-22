package com.syncme.syncme.dto.character;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterCurrentResponse {
    private String date;      // 오늘 날짜
    private Integer score;    // 0~100
    private String state;     // TIRED/NORMAL/ACTIVE
    private String imageKey;  // character_tired/character_normal/character_active
}
