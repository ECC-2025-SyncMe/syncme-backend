package com.syncme.syncme.dto.status;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatusRequest {

    @Min(0) @Max(100)
    private Integer energy;

    @Min(0) @Max(100)
    private Integer burden;

    @Min(0) @Max(100)
    private Integer passion;
}
