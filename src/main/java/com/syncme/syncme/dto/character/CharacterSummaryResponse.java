package com.syncme.syncme.dto.character;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterSummaryResponse {
    private String date;
    private String text;
}
