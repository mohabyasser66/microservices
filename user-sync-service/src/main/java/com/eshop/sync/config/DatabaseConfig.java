package com.eshop.sync.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * Database configuration for multiple data sources:
 * - Application Database (your existing user database)
 * - Keycloak Database (Keycloak's database)
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.eshop.sync.repository.app",
    entityManagerFactoryRef = "appEntityManagerFactory",
    transactionManagerRef = "appTransactionManager"
)
public class DatabaseConfig {

    /**
     * Application Database Configuration (Your existing database)
     */
    @Primary
    @Bean(name = "appDataSource")
    @ConfigurationProperties("spring.datasource.app")
    public DataSource appDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "appEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean appEntityManagerFactory(
            @Qualifier("appDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.eshop.sync.entity.app");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        em.setJpaProperties(properties);

        return em;
    }

    @Primary
    @Bean(name = "appTransactionManager")
    public PlatformTransactionManager appTransactionManager(
            @Qualifier("appEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = "appJdbcTemplate")
    public JdbcTemplate appJdbcTemplate(@Qualifier("appDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Keycloak Database Configuration
     */
    @Bean(name = "keycloakDataSource")
    @ConfigurationProperties("spring.datasource.keycloak")
    public DataSource keycloakDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "keycloakEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean keycloakEntityManagerFactory(
            @Qualifier("keycloakDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.eshop.sync.entity.keycloak");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        em.setJpaProperties(properties);

        return em;
    }

    @Bean(name = "keycloakTransactionManager")
    public PlatformTransactionManager keycloakTransactionManager(
            @Qualifier("keycloakEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "keycloakJdbcTemplate")
    public JdbcTemplate keycloakJdbcTemplate(@Qualifier("keycloakDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
