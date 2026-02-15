package com.syncme.syncme.dto.admin;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDataResponse {
    private TestAccountInfo mainAccount;
    private List<TestAccountInfo> friendAccounts;
    private String message;
    private int daysOfData;
}
