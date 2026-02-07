package com.syncme.syncme.util;

import com.syncme.syncme.dto.character.CharacterState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharacterSummaryTest {

    @Test
    @DisplayName("캐릭터 상태에 따라 요약 문구를 반환한다")
    void summary_success() {
        
        String tired = StatusCalculator.summary(CharacterState.TIRED);
        String normal = StatusCalculator.summary(CharacterState.NORMAL);
        String active = StatusCalculator.summary(CharacterState.ACTIVE);

        assertThat(tired).isNotBlank();
        assertThat(normal).isNotBlank();
        assertThat(active).isNotBlank();
    }
}