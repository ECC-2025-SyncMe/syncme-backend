package com.syncme.syncme.dto.character;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterScoreResponse {
    private String date;
    private Integer score;
}
