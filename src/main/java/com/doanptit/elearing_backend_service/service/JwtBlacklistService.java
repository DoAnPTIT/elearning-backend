package com.doanptit.elearing_backend_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final DynamoDbClient dynamoDbClient;
    private static final String HASH_KEY = "TokenHash";
    private static final String TTL_ATTRIBUTE = "TimeToLive";

    @Value("${app.aws.dynamodb.blacklist-table-name}")
    private String blacklistTableName;

    public void blacklistToken(String token, long expirationSeconds) {
        long ttlValue = Instant.now().getEpochSecond() + expirationSeconds;

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(HASH_KEY, AttributeValue.builder().s(token).build());

        item.put(TTL_ATTRIBUTE, AttributeValue.builder().n(String.valueOf(ttlValue)).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(blacklistTableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    public boolean isTokenBlacklisted(String token) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(blacklistTableName)
                .key(Map.of(HASH_KEY, AttributeValue.builder().s(token).build()))
                .projectionExpression(TTL_ATTRIBUTE)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        return response.hasItem();
    }
}
