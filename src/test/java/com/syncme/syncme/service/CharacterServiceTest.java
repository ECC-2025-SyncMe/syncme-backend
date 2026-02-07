package com.syncme.syncme.service;

import com.syncme.syncme.dto.character.CharacterCurrentResponse;
import com.syncme.syncme.dto.character.CharacterState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CharacterServiceTest {

    @Test
    @DisplayName("오늘 기록이 없을 경우 기본 점수 50점과 NORMAL 상태를 반환한다")
    void getCurrent_noTodayStatus_returnsDefault() {
        
        StatusService statusService = mock(StatusService.class);
        Mockito.when(statusService.getTodayDateString()).thenReturn("2026-01-27");
        Mockito.when(statusService.getTodayEntityOrNull(Mockito.any()))
                .thenReturn(null);

        CharacterService characterService = new CharacterService(statusService);

        CharacterCurrentResponse response = characterService.getCurrent("test@example.com");

        assertThat(response.getScore()).isEqualTo(50);
        assertThat(response.getState()).isEqualTo(CharacterState.NORMAL.name());
    }
}
