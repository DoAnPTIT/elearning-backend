package com.doanptit.elearing_backend_service.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklistService.class);

    private final DynamoDbClient dynamoDbClient;
    private static final String HASH_KEY = "TokenHash";
    private static final String TTL_ATTRIBUTE = "TimeToLive";

    @Value("${app.aws.dynamodb.blacklist-table-name}")
    private String blacklistTableName;

    public void blacklistToken(String token, long expirationSeconds) {
        String ttlValue = String.valueOf(expirationSeconds);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(HASH_KEY, AttributeValue.builder().s(token).build());
        item.put(TTL_ATTRIBUTE, AttributeValue.builder().n(ttlValue).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(blacklistTableName)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(request);
            logger.info("Token đã được thêm vào danh sách đen. TTL: {}", expirationSeconds);
        } catch (SdkClientException e) {
            logger.error("LỖI KẾT NỐI: Không thể thêm token vào danh sách đen DynamoDB.", e);
        } catch (DynamoDbException e) {
            logger.error("LỖI DynamoDB: Không thể thêm token vào danh sách đen. {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(blacklistTableName)
                .key(Map.of(HASH_KEY, AttributeValue.builder().s(token).build()))
                .projectionExpression(HASH_KEY)
                .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(request);
            return response.hasItem();
        } catch (SdkClientException e) {
            logger.error("LỖI KẾT NỐI: Không thể kiểm tra danh sách đen. Cho phép truy cập để đảm bảo dịch vụ không bị gián đoạn.", e);
            return false;
        } catch (DynamoDbException e) {
            logger.error("LỖI DynamoDB: Không thể kiểm tra danh sách đen. {}", e.getMessage());
            return false;
        }
    }
}