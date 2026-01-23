package com.syncme.syncme.dto.calculate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatePreviewResponse {
    private String date;
    private Integer score;
    private String state;
    private String imageKey;
}
