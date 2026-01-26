package com.syncme.syncme.service;

import com.syncme.syncme.dto.character.*;
import com.syncme.syncme.entity.DailyStatus;
import com.syncme.syncme.util.StatusCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final StatusService statusService;

    public CharacterCurrentResponse getCurrent(String email) {
        String date = statusService.getTodayDateString();
        DailyStatus today = statusService.getTodayEntityOrNull(email);

        // 기록이 없을 때 기본값: 50점(NORMAL)
        int score = 50;

        // 기록이 있으면 실제 계산값으로 덮어쓰기
        if (today != null) {
            score = StatusCalculator.calculateScore(
                    today.getEnergy(),
                    today.getBurden(),
                    today.getPassion()
            );
        }

        CharacterState state = StatusCalculator.toState(score);

        return CharacterCurrentResponse.builder()
                .date(date)
                .score(score)
                .state(state.name())
                .imageKey(StatusCalculator.imageKey(state))
                .build();
    }

    public CharacterScoreResponse getScore(String email) {
        return CharacterScoreResponse.builder()
                .date(getCurrent(email).getDate())
                .score(getCurrent(email).getScore())
                .build();
    }

    public CharacterSummaryResponse getSummary(String email) {
        CharacterCurrentResponse current = getCurrent(email);
        CharacterState state = CharacterState.valueOf(current.getState());

        return CharacterSummaryResponse.builder()
                .date(current.getDate())
                .text(StatusCalculator.summary(state))
                .build();
    }
}
