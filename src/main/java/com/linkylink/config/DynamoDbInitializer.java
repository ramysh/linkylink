package com.linkylink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;

/**
 * Automatically creates DynamoDB tables on application startup if they don't exist.
 *
 * This is convenient for development. In production, you might want to create
 * tables manually via the AWS Console or CloudFormation instead.
 *
 * CommandLineRunner: A Spring Boot interface that runs code after the app starts.
 */
@Configuration
public class DynamoDbInitializer {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbInitializer.class);

    @Value("${aws.dynamodb.table.users}")
    private String usersTableName;

    @Value("${aws.dynamodb.table.links}")
    private String linksTableName;

    @Bean
    CommandLineRunner initDynamoDbTables(DynamoDbClient dynamoDbClient) {
        return args -> {
            List<String> existingTables = dynamoDbClient.listTables().tableNames();

            createTableIfNotExists(dynamoDbClient, existingTables, usersTableName, "username");
            createTableIfNotExists(dynamoDbClient, existingTables, linksTableName, "keyword");

            log.info("DynamoDB tables ready!");
        };
    }

    /**
     * Creates a DynamoDB table with a single String partition key.
     *
     * Key concepts:
     *   - Partition Key: The primary way DynamoDB organizes data (like a primary key).
     *   - BillingMode.PAY_PER_REQUEST: You pay only for what you use. No capacity planning needed.
     *     This is perfect for low-traffic personal projects (and stays within free tier).
     */
    private void createTableIfNotExists(DynamoDbClient client, List<String> existingTables,
                                        String tableName, String partitionKeyName) {
        if (existingTables.contains(tableName)) {
            log.info("Table '{}' already exists — skipping creation.", tableName);
            return;
        }

        log.info("Creating DynamoDB table '{}'...", tableName);

        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(KeySchemaElement.builder()
                        .attributeName(partitionKeyName)
                        .keyType(KeyType.HASH) // HASH = Partition Key
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(partitionKeyName)
                        .attributeType(ScalarAttributeType.S) // S = String
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST) // On-demand pricing
                .build();

        client.createTable(request);

        // Wait for the table to become ACTIVE before proceeding
        client.waiter().waitUntilTableExists(b -> b.tableName(tableName));
        log.info("Table '{}' created successfully!", tableName);
    }
}
