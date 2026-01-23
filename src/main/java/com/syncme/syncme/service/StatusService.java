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
            throw new RuntimeException("Today's status already exists");
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

    public void updateToday(String email, UpsertStatusRequest request) {
        String date = todayDate();
        String pk = pk(email);
        String sk = sk(date);

        DailyStatus item = statusRepository.findByPkSk(pk, sk)
                .orElseThrow(() -> new RuntimeException("Today's status not found"));

        item.setEnergy(request.getEnergy());
        item.setBurden(request.getBurden());
        item.setPassion(request.getPassion());
        item.setUpdatedAt(ZonedDateTime.now(KST).toString());

        statusRepository.put(item);
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

    public void deleteAllHistory(String email) {
        statusRepository.deleteAllByPk(pk(email));
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
