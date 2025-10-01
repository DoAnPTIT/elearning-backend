package com.doanptit.elearing_backend_service.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;

@Configuration
@Profile({"local", "dev"})
public class LocalStackDynamoDBConfig {

    private static final String HASH_KEY = "TokenHash";
    private static final String TTL_ATTRIBUTE = "TimeToLive";

    @Value("${app.aws.dynamodb.endpoint}")
    private String dynamoEndpoint;

    @Value("${app.aws.dynamodb.blacklist-table-name}")
    private String blacklistTableName;

    @Value("${app.aws.region}")
    private String awsRegion;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClient client = DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoEndpoint))
                .region(Region.of(awsRegion))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();

        createTableAndEnableTTL(client);

        return client;
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalStackDynamoDBConfig.class);

    private void createTableAndEnableTTL(DynamoDbClient client) {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(blacklistTableName).build());
            logger.info("Bảng {} đã tồn tại, không cần tạo mới.", blacklistTableName);

        } catch (ResourceNotFoundException e) {
            logger.info("Đang tạo bảng DynamoDB Blacklist...");
            try {
                client.createTable(CreateTableRequest.builder()
                        .tableName(blacklistTableName)
                        .keySchema(KeySchemaElement.builder()
                                .attributeName(HASH_KEY)
                                .keyType(KeyType.HASH)
                                .build())
                        .attributeDefinitions(AttributeDefinition.builder()
                                .attributeName(HASH_KEY)
                                .attributeType(ScalarAttributeType.S)
                                .build())
                        .provisionedThroughput(ProvisionedThroughput.builder()
                                .readCapacityUnits(5L).writeCapacityUnits(5L)
                                .build())
                        .build());

                client.waiter().waitUntilTableExists(DescribeTableRequest.builder()
                        .tableName(blacklistTableName)
                        .build());

                client.updateTimeToLive(UpdateTimeToLiveRequest.builder()
                        .tableName(blacklistTableName)
                        .timeToLiveSpecification(TimeToLiveSpecification.builder()
                                .enabled(true)
                                .attributeName(TTL_ATTRIBUTE)
                                .build())
                        .build());
                logger.info("Tạo bảng và TTL hoàn tất.");
            } catch (Exception creationException) {
                logger.error("LỖI CRITICAL khi TẠO BẢNG: {}", creationException.getMessage(), creationException);
            }

        } catch (SdkClientException e) {
            logger.error("SdkClientException: Lỗi kết nối LocalStack (có thể LocalStack chưa sẵn sàng hoặc Endpoint sai): {}", e.getMessage(), e);
            throw e;

        } catch (Exception e) {
            logger.error("Lỗi DynamoDB API khác khi kiểm tra bảng: {}", e.getMessage(), e);
        }
    }
}