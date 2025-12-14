package com.doanptit.elearing_backend_service.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHistoryService {

    private final DynamoDbClient dynamoDbClient;
    private final String TABLE_NAME = "Chat_History";

    // --- PHẦN 1: TỰ ĐỘNG TẠO BẢNG KHI CHẠY APP ---
    @PostConstruct
    public void initTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
            log.info(">>>> DynamoDB: Bảng '{}' đã tồn tại. Sẵn sàng!", TABLE_NAME);
        } catch (ResourceNotFoundException e) {
            log.info(">>>> DynamoDB: Đang tạo bảng '{}'...", TABLE_NAME);
            createTable();
        }
    }

    private void createTable() {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder().attributeName("ConversationId").keyType(KeyType.HASH).build(), // Partition Key
                            KeySchemaElement.builder().attributeName("CreatedAt").keyType(KeyType.RANGE).build()      // Sort Key
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("ConversationId").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("CreatedAt").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build());

            dynamoDbClient.waiter().waitUntilTableExists(DescribeTableRequest.builder().tableName(TABLE_NAME).build());
            log.info(">>>> DynamoDB: Tạo bảng '{}' thành công!", TABLE_NAME);
        } catch (Exception ex) {
            log.error(">>>> DynamoDB Lỗi: Không thể tạo bảng: {}", ex.getMessage());
        }
    }

    // --- PHẦN 2: LOGIC LƯU & LẤY TIN NHẮN ---

    public void saveMessage(String conversationId, String role, String content) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ConversationId", AttributeValue.builder().s(conversationId).build());
        item.put("CreatedAt", AttributeValue.builder().s(Instant.now().toString()).build());
        item.put("Role", AttributeValue.builder().s(role).build());
        item.put("Content", AttributeValue.builder().s(content).build());

        dynamoDbClient.putItem(PutItemRequest.builder().tableName(TABLE_NAME).item(item).build());
    }

    public List<Message> getRecentMessages(String conversationId, int limit) {
        try {
            Map<String, AttributeValue> expressionValues = new HashMap<>();
            expressionValues.put(":pk", AttributeValue.builder().s(conversationId).build());

            QueryResponse response = dynamoDbClient.query(QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .keyConditionExpression("ConversationId = :pk")
                    .expressionAttributeValues(expressionValues)
                    .scanIndexForward(false) // Lấy tin nhắn mới nhất trước
                    .limit(limit)
                    .build());

            List<Message> messages = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                String role = item.get("Role").s();
                String content = item.get("Content").s();

                if ("user".equals(role)) messages.add(new UserMessage(content));
                else messages.add(new AssistantMessage(content));
            }
            // Đảo ngược lại danh sách (Cũ -> Mới) để AI hiểu ngữ cảnh
            Collections.reverse(messages);
            return messages;
        } catch (Exception e) {
            log.error("Lỗi lấy history: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}