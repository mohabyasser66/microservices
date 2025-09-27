package com.eshop.sync.controller;

import com.eshop.sync.service.UserSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User Synchronization operations
 */
@RestController
@RequestMapping("/api/sync")
public class UserSyncController {
    
    @Autowired
    private UserSyncService userSyncService;

    /**
     * Trigger manual full synchronization
     */
    @PostMapping("/full")
    public ResponseEntity<UserSyncService.SyncResult> performFullSync() {
        UserSyncService.SyncResult result = userSyncService.performFullSync();
        return ResponseEntity.ok(result);
    }

    /**
     * Sync from App DB to Keycloak DB only
     */
    @PostMapping("/app-to-keycloak")
    public ResponseEntity<UserSyncService.SyncStats> syncAppToKeycloak() {
        UserSyncService.SyncStats stats = userSyncService.syncAppToKeycloak();
        return ResponseEntity.ok(stats);
    }

    /**
     * Sync from Keycloak DB to App DB only
     */
    @PostMapping("/keycloak-to-app")
    public ResponseEntity<UserSyncService.SyncStats> syncKeycloakToApp() {
        UserSyncService.SyncStats stats = userSyncService.syncKeycloakToApp();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get synchronization status
     */
    @GetMapping("/status")
    public ResponseEntity<UserSyncService.SyncStatus> getSyncStatus() {
        UserSyncService.SyncStatus status = userSyncService.getSyncStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        UserSyncService.SyncStatus status = userSyncService.getSyncStatus();
        if (status.isHealthy()) {
            return ResponseEntity.ok("Sync service is healthy");
        } else {
            return ResponseEntity.status(503).body("Sync service is unhealthy: " + status.getErrorMessage());
        }
    }
}
