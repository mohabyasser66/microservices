package com.eshop.sync.service;

import com.eshop.sync.entity.app.AppUser;
import com.eshop.sync.entity.keycloak.KeycloakCredential;
import com.eshop.sync.entity.keycloak.KeycloakUser;
import com.eshop.sync.repository.app.AppUserRepository;
import com.eshop.sync.repository.keycloak.KeycloakCredentialRepository;
import com.eshop.sync.repository.keycloak.KeycloakUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional User Synchronization Service
 * 
 * This service synchronizes users between your application database and Keycloak database
 * in both directions to maintain consistency.
 */
@Service
public class UserSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSyncService.class);
    
    @Autowired
    private AppUserRepository appUserRepository;
    
    @Autowired
    private KeycloakUserRepository keycloakUserRepository;
    
    @Autowired
    private KeycloakCredentialRepository keycloakCredentialRepository;
    
    @Value("${keycloak.realm-id}")
    private String realmId;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Track last sync time to optimize performance
    private LocalDateTime lastSyncTime = LocalDateTime.now().minusDays(1);

    /**
     * Scheduled synchronization - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void scheduledSync() {
        logger.info("Starting scheduled user synchronization...");
        try {
            syncAppToKeycloak();
            syncKeycloakToApp();
            lastSyncTime = LocalDateTime.now();
            logger.info("Scheduled user synchronization completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled synchronization", e);
        }
    }

    /**
     * Manual synchronization trigger
     */
    @Transactional
    public SyncResult performFullSync() {
        logger.info("Starting full user synchronization...");
        SyncResult result = new SyncResult();
        
        try {
            // Sync from App DB to Keycloak
            SyncStats appToKeycloak = syncAppToKeycloak();
            result.setAppToKeycloakStats(appToKeycloak);
            
            // Sync from Keycloak to App DB
            SyncStats keycloakToApp = syncKeycloakToApp();
            result.setKeycloakToAppStats(keycloakToApp);
            
            result.setSuccess(true);
            result.setMessage("Full synchronization completed successfully");
            
            lastSyncTime = LocalDateTime.now();
            
            logger.info("Full synchronization completed. App->Keycloak: {}, Keycloak->App: {}", 
                       appToKeycloak, keycloakToApp);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Synchronization failed: " + e.getMessage());
            logger.error("Error during full synchronization", e);
        }
        
        return result;
    }

    /**
     * Sync users from Application Database to Keycloak Database
     */
    @Transactional
    public SyncStats syncAppToKeycloak() {
        SyncStats stats = new SyncStats();
        
        // Get modified users since last sync
        List<AppUser> modifiedUsers = appUserRepository.findModifiedAfter(lastSyncTime);
        logger.info("Found {} modified users in app database since {}", modifiedUsers.size(), lastSyncTime);
        
        for (AppUser appUser : modifiedUsers) {
            try {
                syncUserToKeycloak(appUser);
                stats.incrementProcessed();
            } catch (Exception e) {
                logger.error("Error syncing user {} to Keycloak", appUser.getUsername(), e);
                stats.incrementErrors();
            }
        }
        
        return stats;
    }

    /**
     * Sync users from Keycloak Database to Application Database
     */
    @Transactional
    public SyncStats syncKeycloakToApp() {
        SyncStats stats = new SyncStats();
        
        // Get all Keycloak users for this realm
        List<KeycloakUser> keycloakUsers = keycloakUserRepository.findByRealmId(realmId);
        logger.info("Found {} users in Keycloak database for realm {}", keycloakUsers.size(), realmId);
        
        for (KeycloakUser keycloakUser : keycloakUsers) {
            try {
                syncUserToApp(keycloakUser);
                stats.incrementProcessed();
            } catch (Exception e) {
                logger.error("Error syncing user {} to App", keycloakUser.getUsername(), e);
                stats.incrementErrors();
            }
        }
        
        return stats;
    }

    /**
     * Sync a single user from App DB to Keycloak DB
     */
    private void syncUserToKeycloak(AppUser appUser) {
        Optional<KeycloakUser> existingKeycloakUser = keycloakUserRepository.findById(appUser.getId());
        
        if (existingKeycloakUser.isPresent()) {
            // Update existing Keycloak user
            KeycloakUser keycloakUser = existingKeycloakUser.get();
            updateKeycloakUserFromApp(keycloakUser, appUser);
            keycloakUserRepository.save(keycloakUser);
            logger.debug("Updated Keycloak user: {}", appUser.getUsername());
        } else {
            // Create new Keycloak user
            KeycloakUser keycloakUser = createKeycloakUserFromApp(appUser);
            keycloakUserRepository.save(keycloakUser);
            
            // Create password credential
            createPasswordCredential(appUser);
            logger.debug("Created new Keycloak user: {}", appUser.getUsername());
        }
    }

    /**
     * Sync a single user from Keycloak DB to App DB
     */
    private void syncUserToApp(KeycloakUser keycloakUser) {
        Optional<AppUser> existingAppUser = appUserRepository.findById(keycloakUser.getId());
        
        if (existingAppUser.isPresent()) {
            // Update existing App user (only if Keycloak data is newer)
            AppUser appUser = existingAppUser.get();
            if (shouldUpdateAppUser(appUser, keycloakUser)) {
                updateAppUserFromKeycloak(appUser, keycloakUser);
                appUserRepository.save(appUser);
                logger.debug("Updated App user: {}", keycloakUser.getUsername());
            }
        } else {
            // Create new App user (only if user doesn't exist by username/email)
            if (!userExistsInApp(keycloakUser)) {
                AppUser appUser = createAppUserFromKeycloak(keycloakUser);
                appUserRepository.save(appUser);
                logger.debug("Created new App user: {}", keycloakUser.getUsername());
            }
        }
    }

    /**
     * Create Keycloak user from App user
     */
    private KeycloakUser createKeycloakUserFromApp(AppUser appUser) {
        KeycloakUser keycloakUser = new KeycloakUser();
        keycloakUser.setId(appUser.getId());
        keycloakUser.setUsername(appUser.getUsername());
        keycloakUser.setEmail(appUser.getEmail());
        keycloakUser.setFirstName(appUser.getFirstName());
        keycloakUser.setLastName(appUser.getLastName());
        keycloakUser.setEnabled(appUser.getEnabled());
        keycloakUser.setEmailVerified(appUser.getEmailVerified());
        keycloakUser.setRealmId(realmId);
        keycloakUser.setEmailConstraint(appUser.getId());
        keycloakUser.setCreatedTimestamp(System.currentTimeMillis());
        keycloakUser.setNotBefore(0);
        return keycloakUser;
    }

    /**
     * Create App user from Keycloak user
     */
    private AppUser createAppUserFromKeycloak(KeycloakUser keycloakUser) {
        AppUser appUser = new AppUser();
        appUser.setId(keycloakUser.getId());
        appUser.setUsername(keycloakUser.getUsername());
        appUser.setEmail(keycloakUser.getEmail());
        appUser.setFirstName(keycloakUser.getFirstName());
        appUser.setLastName(keycloakUser.getLastName());
        appUser.setEnabled(keycloakUser.getEnabled());
        appUser.setEmailVerified(keycloakUser.getEmailVerified());
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setUpdatedAt(LocalDateTime.now());
        // Note: Password will be empty for users created from Keycloak side
        appUser.setPassword("");
        return appUser;
    }

    /**
     * Update Keycloak user with App user data
     */
    private void updateKeycloakUserFromApp(KeycloakUser keycloakUser, AppUser appUser) {
        keycloakUser.setUsername(appUser.getUsername());
        keycloakUser.setEmail(appUser.getEmail());
        keycloakUser.setFirstName(appUser.getFirstName());
        keycloakUser.setLastName(appUser.getLastName());
        keycloakUser.setEnabled(appUser.getEnabled());
        keycloakUser.setEmailVerified(appUser.getEmailVerified());
        keycloakUser.setEmailConstraint(appUser.getId());
    }

    /**
     * Update App user with Keycloak user data
     */
    private void updateAppUserFromKeycloak(AppUser appUser, KeycloakUser keycloakUser) {
        appUser.setUsername(keycloakUser.getUsername());
        appUser.setEmail(keycloakUser.getEmail());
        appUser.setFirstName(keycloakUser.getFirstName());
        appUser.setLastName(keycloakUser.getLastName());
        appUser.setEnabled(keycloakUser.getEnabled());
        appUser.setEmailVerified(keycloakUser.getEmailVerified());
        appUser.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Create password credential for Keycloak user
     */
    private void createPasswordCredential(AppUser appUser) {
        try {
            String credentialId = UUID.randomUUID().toString();
            
            // Create secret data JSON
            Map<String, Object> secretData = new HashMap<>();
            secretData.put("value", appUser.getPassword());
            secretData.put("salt", "");
            
            // Create credential data JSON (Keycloak format for BCrypt)
            Map<String, Object> credentialData = new HashMap<>();
            credentialData.put("hashIterations", 27500);
            credentialData.put("algorithm", "pbkdf2-sha256");
            
            KeycloakCredential credential = new KeycloakCredential();
            credential.setId(credentialId);
            credential.setType("password");
            credential.setUserId(appUser.getId());
            credential.setSecretData(objectMapper.writeValueAsString(secretData));
            credential.setCredentialData(objectMapper.writeValueAsString(credentialData));
            credential.setCreatedDate(System.currentTimeMillis());
            credential.setUserLabel("password");
            credential.setPriority(10);
            
            keycloakCredentialRepository.save(credential);
            
        } catch (JsonProcessingException e) {
            logger.error("Error creating password credential for user {}", appUser.getUsername(), e);
            throw new RuntimeException("Failed to create password credential", e);
        }
    }

    /**
     * Check if user exists in App database
     */
    private boolean userExistsInApp(KeycloakUser keycloakUser) {
        return appUserRepository.findByUsername(keycloakUser.getUsername()).isPresent() ||
               appUserRepository.findByEmail(keycloakUser.getEmail()).isPresent();
    }

    /**
     * Determine if App user should be updated from Keycloak data
     */
    private boolean shouldUpdateAppUser(AppUser appUser, KeycloakUser keycloakUser) {
        // Simple logic: update if Keycloak user was modified more recently
        // You can enhance this logic based on your requirements
        return appUser.getUpdatedAt().isBefore(
            LocalDateTime.now().minusMinutes(5) // Only if not updated in last 5 minutes
        );
    }

    /**
     * Get synchronization statistics
     */
    public SyncStatus getSyncStatus() {
        SyncStatus status = new SyncStatus();
        
        try {
            long appUserCount = appUserRepository.countActiveUsers();
            long keycloakUserCount = keycloakUserRepository.countActiveUsersByRealm(realmId);
            
            status.setAppUserCount(appUserCount);
            status.setKeycloakUserCount(keycloakUserCount);
            status.setLastSyncTime(lastSyncTime);
            status.setSynced(appUserCount == keycloakUserCount);
            status.setHealthy(true);
            
        } catch (Exception e) {
            logger.error("Error getting sync status", e);
            status.setHealthy(false);
            status.setErrorMessage(e.getMessage());
        }
        
        return status;
    }

    // Inner classes for result objects
    public static class SyncResult {
        private boolean success;
        private String message;
        private SyncStats appToKeycloakStats;
        private SyncStats keycloakToAppStats;
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public SyncStats getAppToKeycloakStats() { return appToKeycloakStats; }
        public void setAppToKeycloakStats(SyncStats appToKeycloakStats) { this.appToKeycloakStats = appToKeycloakStats; }
        public SyncStats getKeycloakToAppStats() { return keycloakToAppStats; }
        public void setKeycloakToAppStats(SyncStats keycloakToAppStats) { this.keycloakToAppStats = keycloakToAppStats; }
    }

    public static class SyncStats {
        private int processed = 0;
        private int errors = 0;
        
        public void incrementProcessed() { processed++; }
        public void incrementErrors() { errors++; }
        
        public int getProcessed() { return processed; }
        public int getErrors() { return errors; }
        
        @Override
        public String toString() {
            return String.format("SyncStats{processed=%d, errors=%d}", processed, errors);
        }
    }

    public static class SyncStatus {
        private long appUserCount;
        private long keycloakUserCount;
        private LocalDateTime lastSyncTime;
        private boolean synced;
        private boolean healthy;
        private String errorMessage;
        
        // Getters and setters
        public long getAppUserCount() { return appUserCount; }
        public void setAppUserCount(long appUserCount) { this.appUserCount = appUserCount; }
        public long getKeycloakUserCount() { return keycloakUserCount; }
        public void setKeycloakUserCount(long keycloakUserCount) { this.keycloakUserCount = keycloakUserCount; }
        public LocalDateTime getLastSyncTime() { return lastSyncTime; }
        public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
        public boolean isSynced() { return synced; }
        public void setSynced(boolean synced) { this.synced = synced; }
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
