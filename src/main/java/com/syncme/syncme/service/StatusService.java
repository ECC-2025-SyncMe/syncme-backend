package com.syncme.syncme.service;

import com.syncme.syncme.dto.status.*;
import com.syncme.syncme.entity.DailyStatus;
import com.syncme.syncme.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private String todayDate() {
        return ZonedDateTime.now(KST).toLocalDate().toString(); // YYYY-MM-DD
    }

    private String pk(String email) {
        return "USER#" + email;
    }

    private String sk(String date) {
        return "DATE#" + date;
    }

    public TodayStatusResponse getToday(String email) {
        String date = todayDate();
        String pk = pk(email);
        String sk = sk(date);

        return statusRepository.findByPkSk(pk, sk)
                .map(s -> TodayStatusResponse.builder()
                        .date(date)
                        .energy(s.getEnergy())
                        .burden(s.getBurden())
                        .passion(s.getPassion())
                        .exists(true)
                        .build())
                .orElseGet(() -> TodayStatusResponse.builder()
                        .date(date)
                        .energy(null)
                        .burden(null)
                        .passion(null)
                        .exists(false)
                        .build());
    }

    
    public void createToday(String email, UpsertStatusRequest request) {
        String date = todayDate();
        String pk = pk(email);
        String sk = sk(date);

        if (statusRepository.exists(pk, sk)) {
            throw new RuntimeException("작성된 기록이 있습니다!");
        }

        String now = ZonedDateTime.now(KST).toString();

        DailyStatus item = new DailyStatus();
        item.setPk(pk);
        item.setSk(sk);
        item.setEnergy(request.getEnergy());
        item.setBurden(request.getBurden());
        item.setPassion(request.getPassion());
        item.setCreatedAt(now);
        item.setUpdatedAt(now);

        statusRepository.put(item);
    }

    public void updateTodayPartial(String email, UpdateStatusRequest request) {
        String pk = buildPk(email);
        String sk = buildSk(getTodayDateString());

        DailyStatus today = statusRepository.findByPkSk(pk, sk)
                .orElseThrow(() -> new RuntimeException("기록이 없습니다."));

    
        if (request.getEnergy() != null) {
            today.setEnergy(request.getEnergy());
        }
        if (request.getBurden() != null) {
            today.setBurden(request.getBurden());
        }
        if (request.getPassion() != null) {
            today.setPassion(request.getPassion());
        }
        statusRepository.put(today);
    }

    public StatusCheckResponse checkToday(String email) {
        String date = todayDate();
        boolean exists = statusRepository.exists(pk(email), sk(date));

        return StatusCheckResponse.builder()
                .date(date)
                .exists(exists)
                .build();
    }

    public HistoryListResponse getHistory(String email) {
        List<DailyStatus> list = statusRepository.findAllByPkOrderBySkDesc(pk(email));

        List<HistoryListResponse.Item> items = list.stream()
                .map(s -> HistoryListResponse.Item.builder()
                        .date(s.getSk().replace("DATE#", ""))
                        .energy(s.getEnergy())
                        .burden(s.getBurden())
                        .passion(s.getPassion())
                        .build())
                .toList();

        return HistoryListResponse.builder()
                .count(items.size())
                .items(items)
                .build();
    }

    public HistoryListResponse.Item getHistoryByDate(String email, String date) {
        DailyStatus item = statusRepository.findByPkSk(pk(email), sk(date))
                .orElseThrow(() -> new RuntimeException("Status not found for date: " + date));

        return HistoryListResponse.Item.builder()
                .date(date)
                .energy(item.getEnergy())
                .burden(item.getBurden())
                .passion(item.getPassion())
                .build();
    }

    public void deleteHistoryByDate(String email, String date) {
        String pk = buildPk(email);
        String sk = buildSk(date);
        if (!statusRepository.exists(pk, sk)) {
            throw new RuntimeException("기록이 없습니다.");
        }
        statusRepository.deleteByPkSk(pk, sk);
    }

    // 캐릭터/계산에서 사용
    public DailyStatus getTodayEntityOrNull(String email) {
        String date = todayDate();
        return statusRepository.findByPkSk(pk(email), sk(date)).orElse(null);
    }

    public String getTodayDateString() {
        return todayDate();
    }
}
