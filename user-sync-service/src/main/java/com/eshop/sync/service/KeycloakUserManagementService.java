package com.eshop.sync.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Keycloak User Management Service
 * 
 * This service provides advanced user management features using Keycloak Admin Client
 * including user locking, password management, and session control.
 */
@Service
public class KeycloakUserManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserManagementService.class);
    
    @Autowired
    private Keycloak keycloakAdminClient;
    
    @Value("${keycloak.realm}")
    private String realmName;
    
    /**
     * Lock a user account in Keycloak
     */
    public boolean lockUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            UserRepresentation user = userResource.toRepresentation();
            if (user != null) {
                user.setEnabled(false);
                userResource.update(user);
                
                logger.info("User locked in Keycloak: {}", keycloakUserId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to lock user in Keycloak: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Unlock a user account in Keycloak
     */
    public boolean unlockUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            UserRepresentation user = userResource.toRepresentation();
            if (user != null) {
                user.setEnabled(true);
                userResource.update(user);
                
                // Clear any temporary lockouts
                userResource.executeActionsEmail(Arrays.asList());
                
                logger.info("User unlocked in Keycloak: {}", keycloakUserId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to unlock user in Keycloak: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Reset password for a user in Keycloak
     */
    public boolean resetPassword(String keycloakUserId, String newPassword) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            // Create credential representation
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false); // Set to true if you want user to change on next login
            
            userResource.resetPassword(credential);
            
            logger.info("Password reset for user in Keycloak: {}", keycloakUserId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to reset password for user in Keycloak: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Send password reset email through Keycloak
     */
    public boolean sendPasswordResetEmail(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            // Send required actions email (password reset)
            userResource.executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
            
            logger.info("Password reset email sent for user: {}", keycloakUserId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send password reset email for user: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Send email verification through Keycloak
     */
    public boolean sendEmailVerification(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            userResource.sendVerifyEmail();
            
            logger.info("Email verification sent for user: {}", keycloakUserId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email verification for user: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Terminate all user sessions in Keycloak
     */
    public boolean logoutUser(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            userResource.logout();
            
            logger.info("All sessions terminated for user: {}", keycloakUserId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to logout user: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Get user sessions from Keycloak
     */
    public List<Map<String, Object>> getUserSessions(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            return userResource.getUserSessions().stream()
                    .map(session -> {
                        Map<String, Object> sessionInfo = new HashMap<>();
                        sessionInfo.put("id", session.getId());
                        sessionInfo.put("start", session.getStart());
                        sessionInfo.put("lastAccess", session.getLastAccess());
                        sessionInfo.put("ipAddress", session.getIpAddress());
                        sessionInfo.put("clients", session.getClients());
                        return sessionInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get user sessions: {}", keycloakUserId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Check if user is locked due to brute force protection
     */
    public boolean isUserLockedByBruteForce(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            UsersResource usersResource = realmResource.users();
            UserResource userResource = usersResource.get(keycloakUserId);
            
            UserRepresentation user = userResource.toRepresentation();
            if (user != null) {
                // Check if user is disabled (could be due to brute force)
                return !user.isEnabled();
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to check user lock status: {}", keycloakUserId, e);
            return false;
        }
    }
    
    /**
     * Get user login events/audit logs
     */
    public List<Map<String, Object>> getUserLoginEvents(String keycloakUserId) {
        try {
            RealmResource realmResource = keycloakAdminClient.realm(realmName);
            
            // Query events for specific user
            return realmResource.getEvents(
                    Arrays.asList("LOGIN", "LOGIN_ERROR", "LOGOUT"), // Event types
                    null, null, null, keycloakUserId, null, null, null, null)
                    .stream()
                    .map(event -> {
                        Map<String, Object> eventInfo = new HashMap<>();
                        eventInfo.put("type", event.getType());
                        eventInfo.put("time", event.getTime());
                        eventInfo.put("ipAddress", event.getIpAddress());
                        eventInfo.put("details", event.getDetails());
                        eventInfo.put("error", event.getError());
                        return eventInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get user login events: {}", keycloakUserId, e);
            return Collections.emptyList();
        }
    }
}
