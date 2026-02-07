package com.syncme.syncme.util;

import com.syncme.syncme.dto.character.CharacterState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusCalculatorTest {

    @Test
    @DisplayName("에너지/부담감/열정 입력값으로 100점 만점 점수를 계산한다")
    void calculateScore_success() {
        
        int energy = 80;
        int burden = 30;
        int passion = 70;

        int score = StatusCalculator.calculateScore(energy, burden, passion);

        assertThat(score).isBetween(0, 100);
    }

    @Test
    @DisplayName("점수 구간에 따라 캐릭터 상태를 올바르게 반환한다")
    void toState_success() {
        assertThat(StatusCalculator.toState(20)).isEqualTo(CharacterState.TIRED);
        assertThat(StatusCalculator.toState(50)).isEqualTo(CharacterState.NORMAL);
        assertThat(StatusCalculator.toState(90)).isEqualTo(CharacterState.ACTIVE);
    }
}