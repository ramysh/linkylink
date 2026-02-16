package com.linkylink.repository;

import com.linkylink.model.Link;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.List;
import java.util.Map;

/**
 * Repository for Link CRUD operations against DynamoDB.
 *
 * Uses two clients:
 *   - Enhanced Client: For standard CRUD (get, put, delete, scan)
 *   - Low-level Client: For the atomic click counter increment
 */
@Repository
public class LinkRepository {

    private final DynamoDbTable<Link> table;
    private final DynamoDbClient lowLevelClient;
    private final String tableName;

    public LinkRepository(DynamoDbEnhancedClient enhancedClient,
                            DynamoDbClient lowLevelClient,
                            @Value("${aws.dynamodb.table.links}") String tableName) {
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(Link.class));
        this.lowLevelClient = lowLevelClient;
        this.tableName = tableName;
    }

    /**
     * Find a go link by keyword (partition key lookup — very fast).
     */
    public Link findByKeyword(String keyword) {
        return table.getItem(Key.builder().partitionValue(keyword).build());
    }

    /**
     * Save a go link (creates new or overwrites existing).
     */
    public void save(Link link) {
        table.putItem(link);
    }

    /**
     * Delete a go link by keyword.
     */
    public void delete(String keyword) {
        table.deleteItem(Key.builder().partitionValue(keyword).build());
    }

    /**
     * Get ALL go links (full table scan).
     */
    public List<Link> findAll() {
        return table.scan().items().stream().toList();
    }

    /**
     * Find all go links owned by a specific user.
     *
     * Uses a scan with a filter expression. This reads every item in the table
     * and filters on the server side. Fine for small tables (<1000 items).
     *
     * For better performance at scale, you'd add a Global Secondary Index (GSI)
     * on ownerUsername — but for a personal project, scan is perfectly fine.
     */
    public List<Link> findByOwner(String username) {
        Expression filterExpression = Expression.builder()
                .expression("ownerUsername = :owner")
                .putExpressionValue(":owner", AttributeValue.builder().s(username).build())
                .build();

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return table.scan(request).items().stream().toList();
    }

    /**
     * Atomically increment the click counter for a go link.
     *
     * Uses the low-level client because the Enhanced Client doesn't support
     * atomic counter increments. This is a DynamoDB "Update Expression".
     *
     * "SET clickCount = if_not_exists(clickCount, :zero) + :one"
     * means: "If clickCount exists, add 1. If it doesn't exist yet, start at 0 + 1."
     *
     * This is atomic — even if two people click at the exact same time,
     * both clicks are counted correctly (no race condition).
     */
    public void incrementClickCount(String keyword) {
        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("keyword", AttributeValue.builder().s(keyword).build()))
                .updateExpression("SET clickCount = if_not_exists(clickCount, :zero) + :one")
                .expressionAttributeValues(Map.of(
                        ":zero", AttributeValue.builder().n("0").build(),
                        ":one", AttributeValue.builder().n("1").build()))
                .build();

        lowLevelClient.updateItem(request);
    }
}
