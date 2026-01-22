package com.syncme.syncme.dto.status;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusCheckResponse {
    private String date;
    private boolean exists;
}
