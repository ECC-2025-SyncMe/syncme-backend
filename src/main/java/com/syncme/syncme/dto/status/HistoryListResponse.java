package com.syncme.syncme.dto.status;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryListResponse {

    private int count;
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String date;     // YYYY-MM-DD
        private Integer energy;
        private Integer burden;
        private Integer passion;
    }
}
