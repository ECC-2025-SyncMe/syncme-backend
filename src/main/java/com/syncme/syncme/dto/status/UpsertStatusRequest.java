package com.syncme.syncme.dto.status;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpsertStatusRequest {

    @NotNull @Min(0) @Max(100)
    private Integer energy;

    @NotNull @Min(0) @Max(100)
    private Integer burden;

    @NotNull @Min(0) @Max(100)
    private Integer passion;
}
