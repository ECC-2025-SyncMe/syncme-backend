package com.syncme.syncme.repository;

import com.syncme.syncme.entity.DailyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StatusRepository {

    private final DynamoDbEnhancedClient enhancedClient;

    @Value("${dynamodb.table.status:syncme_daily_status}")
    private String tableName;

    private DynamoDbTable<DailyStatus> table() {
        return enhancedClient.table(tableName, TableSchema.fromBean(DailyStatus.class));
    }

    public Optional<DailyStatus> findByPkSk(String pk, String sk) {
        DailyStatus item = table().getItem(Key.builder().partitionValue(pk).sortValue(sk).build());
        return Optional.ofNullable(item);
    }

    public boolean exists(String pk, String sk) {
        return findByPkSk(pk, sk).isPresent();
    }

    public void put(DailyStatus item) {
        table().putItem(item);
    }

    public List<DailyStatus> findAllByPkOrderBySkDesc(String pk) {
        List<DailyStatus> result = new ArrayList<>();

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(pk)))
                .scanIndexForward(false) // SK 내림차순
                .build();

        for (Page<DailyStatus> page : table().query(request)) {
            result.addAll(page.items());
        }
        return result;
    }

    public void deleteByPkSk(String pk, String sk) {
        table().deleteItem(Key.builder().partitionValue(pk).sortValue(sk).build());
    }

    public void deleteAllByPk(String pk) {
        List<DailyStatus> items = findAllByPkOrderBySkDesc(pk);
        if (items.isEmpty()) return;

        // BatchWrite로 삭제 (25개 단위)
        DynamoDbTable<DailyStatus> table = table();
        int i = 0;
        while (i < items.size()) {
            int end = Math.min(i + 25, items.size());

            WriteBatch.Builder<DailyStatus> wb = WriteBatch.builder(DailyStatus.class)
                    .mappedTableResource(table);

            for (DailyStatus it : items.subList(i, end)) {
                wb.addDeleteItem(Key.builder()
                        .partitionValue(it.getPk())
                        .sortValue(it.getSk())
                        .build());
            }

            BatchWriteItemEnhancedRequest req = BatchWriteItemEnhancedRequest.builder()
                    .writeBatches(wb.build())
                    .build();

            enhancedClient.batchWriteItem(req);
            i = end;
        }
    }
}

// DynamoDB 테이블명은 syncme_daily_status 사용