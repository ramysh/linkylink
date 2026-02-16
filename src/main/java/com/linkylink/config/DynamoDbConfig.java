package com.linkylink.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.net.URI;

/**
 * Configures the AWS DynamoDB clients used throughout the application.
 *
 * Two clients are created:
 *   1. DynamoDbClient         — low-level client (for atomic updates like click counters)
 *   2. DynamoDbEnhancedClient — high-level ORM-like client (for CRUD operations)
 *
 * Authentication:
 *   - On EC2: Uses an IAM Role attached to the instance (no keys needed in code!)
 *   - Locally: Uses credentials from `aws configure` (~/.aws/credentials)
 *   DefaultCredentialsProvider automatically picks the right method.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.dynamodb.endpoint:}")
    private String endpoint;

    /**
     * Low-level DynamoDB client.
     * Used for operations that the Enhanced Client doesn't support,
     * like atomic counter increments.
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        // If an endpoint is configured (e.g., DynamoDB Local), override the default AWS endpoint
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * Enhanced DynamoDB client.
     * Provides an ORM-like experience: map Java classes directly to DynamoDB tables.
     * Think of it like JPA/Hibernate but for DynamoDB.
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
