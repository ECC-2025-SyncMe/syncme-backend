package com.syncme.syncme.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateNicknameRequest {
    
    @NotBlank(message = "Nickname is required")
    @Size(max = 50, message = "Nickname must be less than 50 characters")
    private String nickname;
}
