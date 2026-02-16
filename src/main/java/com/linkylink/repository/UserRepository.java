package com.linkylink.repository;

import com.linkylink.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;

/**
 * Repository for User CRUD operations against DynamoDB.
 *
 * This is similar to a JPA Repository, but using the DynamoDB Enhanced Client.
 * The Enhanced Client maps Java beans to DynamoDB items automatically.
 *
 * Key DynamoDB concepts:
 *   - getItem:  Fetch one item by its primary key (fast, O(1))
 *   - putItem:  Insert or replace an item
 *   - deleteItem: Remove an item by its primary key
 *   - scan:     Read ALL items in the table (slow for large tables, fine for small ones)
 */
@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient enhancedClient,
                          @Value("${aws.dynamodb.table.users}") String tableName) {
        // Create a table reference: maps the User class to the DynamoDB table
        this.table = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    /**
     * Find a user by username (partition key lookup — very fast).
     * Returns null if not found.
     */
    public User findByUsername(String username) {
        return table.getItem(Key.builder().partitionValue(username).build());
    }

    /**
     * Save a user (creates new or overwrites existing).
     */
    public void save(User user) {
        table.putItem(user);
    }

    /**
     * Delete a user by username.
     */
    public void delete(String username) {
        table.deleteItem(Key.builder().partitionValue(username).build());
    }

    /**
     * Get all users (full table scan).
     * Fine for small tables (<1000 items). For large tables, use pagination.
     */
    public List<User> findAll() {
        return table.scan().items().stream().toList();
    }

    /**
     * Check if any users exist in the table.
     * Used to determine if the first registering user should be made ADMIN.
     */
    public boolean isEmpty() {
        // Scan with a limit of 1 — we just need to know if there's at least one item
        return table.scan(b -> b.limit(1)).items().stream().findFirst().isEmpty();
    }
}
