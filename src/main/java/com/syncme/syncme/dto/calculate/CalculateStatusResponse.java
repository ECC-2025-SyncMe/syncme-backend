package com.syncme.syncme.dto.calculate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateStatusResponse {
    private Integer score;
    private String state;
    private String imageKey;
    private String summary;
}
