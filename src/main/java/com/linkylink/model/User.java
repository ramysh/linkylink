package com.linkylink.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * User entity mapped to the "LinkyLinkUsers" DynamoDB table.
 *
 * DynamoDB Enhanced Client annotations:
 *   @DynamoDbBean      — Marks this class as a DynamoDB-mapped object (like JPA's @Entity)
 *   @DynamoDbPartitionKey — Marks the primary key field (like JPA's @Id)
 *
 * DynamoDB requires:
 *   - A public no-arg constructor
 *   - Public getter and setter for every attribute
 *   - The partition key annotation on the GETTER (not the field)
 *
 * Table structure:
 *   | username (PK) | passwordHash | role   | createdAt          |
 *   |---------------|-------------|--------|--------------------|
 *   | alice         | $2a$10$...  | ADMIN  | 2025-01-15T10:30   |
 *   | bob           | $2a$10$...  | USER   | 2025-01-16T14:00   |
 */
@DynamoDbBean
public class User {

    private String username;
    private String passwordHash;
    private String role;        // "USER" or "ADMIN"
    private String createdAt;   // ISO 8601 timestamp

    // === No-arg constructor (required by DynamoDB Enhanced Client) ===
    public User() {
    }

    public User(String username, String passwordHash, String role, String createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // === Partition Key ===
    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // === Other attributes ===
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
