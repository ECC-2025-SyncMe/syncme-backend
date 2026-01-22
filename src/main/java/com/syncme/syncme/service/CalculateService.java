package com.syncme.syncme.service;

import com.syncme.syncme.dto.calculate.CalculatePreviewResponse;
import com.syncme.syncme.dto.calculate.CalculateStatusResponse;
import com.syncme.syncme.dto.character.CharacterState;
import com.syncme.syncme.dto.status.UpsertStatusRequest;
import com.syncme.syncme.entity.DailyStatus;
import com.syncme.syncme.util.StatusCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculateService {

    private final StatusService statusService;

    // 입력값 기반 계산(저장 X)
    public CalculateStatusResponse calculate(UpsertStatusRequest request) {
        int score = StatusCalculator.calculateScore(request.getEnergy(), request.getBurden(), request.getPassion());
        CharacterState state = StatusCalculator.toState(score);

        return CalculateStatusResponse.builder()
                .score(score)
                .state(state.name())
                .imageKey(StatusCalculator.imageKey(state))
                .summary(StatusCalculator.summary(state))
                .build();
    }

    // 저장된 오늘 상태 기준 프리뷰(명세 유지)
    public CalculatePreviewResponse preview(String email) {
        String date = statusService.getTodayDateString();
        DailyStatus today = statusService.getTodayEntityOrNull(email);

        int score = 0;
        if (today != null) {
            score = StatusCalculator.calculateScore(today.getEnergy(), today.getBurden(), today.getPassion());
        }

        CharacterState state = StatusCalculator.toState(score);

        return CalculatePreviewResponse.builder()
                .date(date)
                .score(score)
                .state(state.name())
                .imageKey(StatusCalculator.imageKey(state))
                .build();
    }
}
