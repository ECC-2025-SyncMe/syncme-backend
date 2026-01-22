package com.syncme.syncme.controller;

import com.syncme.syncme.dto.character.CharacterCurrentResponse;
import com.syncme.syncme.dto.character.CharacterScoreResponse;
import com.syncme.syncme.dto.character.CharacterSummaryResponse;
import com.syncme.syncme.dto.common.ApiResponse;
import com.syncme.syncme.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/character")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<CharacterCurrentResponse>> current(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CharacterCurrentResponse response = characterService.getCurrent(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/score")
    public ResponseEntity<ApiResponse<CharacterScoreResponse>> score(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CharacterScoreResponse response = characterService.getScore(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CharacterSummaryResponse>> summary(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CharacterSummaryResponse response = characterService.getSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
