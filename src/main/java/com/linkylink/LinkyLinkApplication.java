package com.linkylink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the LinkyLinks application.
 *
 * @SpringBootApplication combines three annotations:
 *   - @Configuration:       This class can define Spring beans
 *   - @EnableAutoConfiguration: Spring Boot auto-configures based on dependencies
 *   - @ComponentScan:       Scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class LinkyLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkyLinkApplication.class, args);
    }
}
