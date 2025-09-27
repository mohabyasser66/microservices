package com.eshop.sync.controller;

import com.eshop.sync.service.KeycloakUserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keycloak User Management API Controller
 * 
 * Provides REST endpoints for advanced user management features
 * using Keycloak's native capabilities.
 */
@RestController
@RequestMapping("/api/keycloak/users")
@CrossOrigin(origins = "*")
public class KeycloakUserController {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserController.class);
    
    @Autowired
    private KeycloakUserManagementService keycloakUserManagementService;
    
    /**
     * Lock a user account in Keycloak
     */
    @PostMapping("/{keycloakUserId}/lock")
    public ResponseEntity<Map<String, Object>> lockUser(@PathVariable String keycloakUserId) {
        logger.info("Request to lock user: {}", keycloakUserId);
        
        boolean success = keycloakUserManagementService.lockUser(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "User locked successfully" : "Failed to lock user");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Unlock a user account in Keycloak
     */
    @PostMapping("/{keycloakUserId}/unlock")
    public ResponseEntity<Map<String, Object>> unlockUser(@PathVariable String keycloakUserId) {
        logger.info("Request to unlock user: {}", keycloakUserId);
        
        boolean success = keycloakUserManagementService.unlockUser(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "User unlocked successfully" : "Failed to unlock user");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Reset user password in Keycloak
     */
    @PostMapping("/{keycloakUserId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable String keycloakUserId,
            @RequestBody Map<String, String> request) {
        
        logger.info("Request to reset password for user: {}", keycloakUserId);
        
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "New password is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        boolean success = keycloakUserManagementService.resetPassword(keycloakUserId, newPassword);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Password reset successfully" : "Failed to reset password");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Send password reset email through Keycloak
     */
    @PostMapping("/{keycloakUserId}/send-password-reset")
    public ResponseEntity<Map<String, Object>> sendPasswordResetEmail(@PathVariable String keycloakUserId) {
        logger.info("Request to send password reset email for user: {}", keycloakUserId);
        
        boolean success = keycloakUserManagementService.sendPasswordResetEmail(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Password reset email sent successfully" : "Failed to send password reset email");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Send email verification through Keycloak
     */
    @PostMapping("/{keycloakUserId}/send-email-verification")
    public ResponseEntity<Map<String, Object>> sendEmailVerification(@PathVariable String keycloakUserId) {
        logger.info("Request to send email verification for user: {}", keycloakUserId);
        
        boolean success = keycloakUserManagementService.sendEmailVerification(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Email verification sent successfully" : "Failed to send email verification");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Terminate all user sessions (logout user from all devices)
     */
    @PostMapping("/{keycloakUserId}/logout")
    public ResponseEntity<Map<String, Object>> logoutUser(@PathVariable String keycloakUserId) {
        logger.info("Request to logout user from all sessions: {}", keycloakUserId);
        
        boolean success = keycloakUserManagementService.logoutUser(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "User logged out from all sessions" : "Failed to logout user");
        response.put("keycloakUserId", keycloakUserId);
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Get user sessions from Keycloak
     */
    @GetMapping("/{keycloakUserId}/sessions")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable String keycloakUserId) {
        logger.info("Request to get sessions for user: {}", keycloakUserId);
        
        List<Map<String, Object>> sessions = keycloakUserManagementService.getUserSessions(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keycloakUserId", keycloakUserId);
        response.put("sessions", sessions);
        response.put("sessionCount", sessions.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user is locked due to brute force protection
     */
    @GetMapping("/{keycloakUserId}/lock-status")
    public ResponseEntity<Map<String, Object>> getUserLockStatus(@PathVariable String keycloakUserId) {
        logger.info("Request to check lock status for user: {}", keycloakUserId);
        
        boolean isLocked = keycloakUserManagementService.isUserLockedByBruteForce(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keycloakUserId", keycloakUserId);
        response.put("isLocked", isLocked);
        response.put("message", isLocked ? "User account is locked" : "User account is not locked");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user login events and audit logs
     */
    @GetMapping("/{keycloakUserId}/login-events")
    public ResponseEntity<Map<String, Object>> getUserLoginEvents(@PathVariable String keycloakUserId) {
        logger.info("Request to get login events for user: {}", keycloakUserId);
        
        List<Map<String, Object>> events = keycloakUserManagementService.getUserLoginEvents(keycloakUserId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("keycloakUserId", keycloakUserId);
        response.put("events", events);
        response.put("eventCount", events.size());
        
        return ResponseEntity.ok(response);
    }
}
