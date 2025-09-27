package com.eshop.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * User Synchronization Service
 * 
 * This service provides bidirectional synchronization between your application database
 * and Keycloak database to keep user data consistent across both systems.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableTransactionManagement
public class UserSyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSyncServiceApplication.class, args);
    }
}
