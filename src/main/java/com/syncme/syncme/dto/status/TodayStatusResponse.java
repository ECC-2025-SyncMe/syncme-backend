package com.syncme.syncme.dto.status;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayStatusResponse {
    private String date;
    private Integer energy;
    private Integer burden;
    private Integer passion;
    private boolean exists;
}
