package com.eshop.sync.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Keycloak Database JPA Configuration
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.eshop.sync.repository.keycloak",
    entityManagerFactoryRef = "keycloakEntityManagerFactory",
    transactionManagerRef = "keycloakTransactionManager"
)
public class KeycloakDatabaseConfig {
    // Configuration is handled in DatabaseConfig.java
}
