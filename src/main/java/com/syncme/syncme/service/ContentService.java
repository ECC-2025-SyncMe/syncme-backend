package com.syncme.syncme.service;

import com.syncme.syncme.dto.content.ContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ContentService {
    
    private final Random random = new Random();
    
    private final List<String> todayMessages = Arrays.asList(
            "오늘도 화이팅!",
            "좋은 하루 되세요!",
            "당신은 할 수 있어요!",
            "오늘의 목표를 이뤄봐요!",
            "작은 성취도 큰 변화예요!",
            "한 걸음씩 나아가요!",
            "오늘도 최선을 다해요!",
            "긍정적인 마음으로 시작해요!",
            "당신의 노력은 빛날 거예요!",
            "새로운 하루, 새로운 시작!"
    );
    
    private final List<String> loadingMessages = Arrays.asList(
            "잠시만 기다려주세요...",
            "데이터를 불러오는 중...",
            "캐릭터를 준비하는 중...",
            "거의 다 됐어요!",
            "조금만 더 기다려주세요..."
    );
    
    public ContentResponse getTodayMessage() {
        String message = todayMessages.get(random.nextInt(todayMessages.size()));
        return ContentResponse.builder()
                .message(message)
                .build();
    }
    
    public ContentResponse getLoadingMessage() {
        String message = loadingMessages.get(random.nextInt(loadingMessages.size()));
        return ContentResponse.builder()
                .message(message)
                .build();
    }
    
    public ContentResponse getAbout() {
        String aboutMessage = "SyncMe는 일상의 상태를 기록하고 캐릭터와 함께 성장하는 서비스입니다. " +
                "매일의 에너지, 부담감, 열정을 기록하면 캐릭터가 변화하며 당신의 상태를 시각적으로 보여줍니다. " +
                "Version 1.0.0";
        
        return ContentResponse.builder()
                .message(aboutMessage)
                .build();
    }
}
