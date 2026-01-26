package com.syncme.syncme.util;

import com.syncme.syncme.dto.character.CharacterState;

public class StatusCalculator {

    private StatusCalculator() {}

    /**
     * 에너지(+) / 부담감(-) / 열정(+)
     * 0~100 점수로 환산
     */
    public static int calculateScore(int energy, int burden, int passion) {
        int score = (int) Math.round(0.4 * energy + 0.4 * passion + 0.2 * (100 - burden));
        return clamp(score, 0, 100);
    }

    public static CharacterState toState(int score) {
        if (score <= 30) return CharacterState.TIRED;
        if (score <= 70) return CharacterState.NORMAL;
        return CharacterState.ACTIVE;
    }

    public static String imageKey(CharacterState state) {
        return switch (state) {
            case TIRED -> "character_tired";
            case NORMAL -> "character_normal";
            case ACTIVE -> "character_active";
        };
    }

    public static String summary(CharacterState state) {
        return switch (state) {
            case TIRED -> "잠깐 쉬어가도 괜찮아요!";
            case NORMAL -> "무난하게 잘 버텼어요!";
            case ACTIVE -> "오늘은 활력이 넘쳐요!";
        };
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
