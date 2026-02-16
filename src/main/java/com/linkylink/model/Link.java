package com.linkylink.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Link entity mapped to the "LinkyLinks" DynamoDB table.
 *
 * This is the core of the app: each row maps a short keyword to a full URL.
 *
 * Table structure:
 *   | keyword (PK) | url                        | ownerUsername | description    | createdAt          | clickCount |
 *   |-------------|----------------------------|-------------|----------------|--------------------|-----------:|
 *   | google      | https://www.google.com     | alice       | Google search  | 2025-01-15T10:30   |        42 |
 *   | gh          | https://github.com         | bob         | GitHub         | 2025-01-16T14:00   |        17 |
 *   | jira        | https://mycompany.jira.com | alice       | Our Jira board | 2025-01-17T09:15   |       128 |
 */
@DynamoDbBean
public class Link {

    private String keyword;
    private String url;
    private String ownerUsername;
    private String description;
    private String createdAt;
    private Long clickCount;

    // === No-arg constructor (required by DynamoDB Enhanced Client) ===
    public Link() {
    }

    public Link(String keyword, String url, String ownerUsername,
                  String description, String createdAt) {
        this.keyword = keyword;
        this.url = url;
        this.ownerUsername = ownerUsername;
        this.description = description;
        this.createdAt = createdAt;
        this.clickCount = 0L;
    }

    // === Partition Key ===
    @DynamoDbPartitionKey
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    // === Other attributes ===
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
}
