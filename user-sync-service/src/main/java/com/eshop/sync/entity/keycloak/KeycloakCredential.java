package com.eshop.sync.entity.keycloak;

import jakarta.persistence.*;

/**
 * Keycloak Credential Entity for storing password hashes
 */
@Entity
@Table(name = "credential")
public class KeycloakCredential {
    
    @Id
    private String id;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "created_date")
    private Long createdDate;
    
    @Column(name = "user_label")
    private String userLabel;
    
    @Column(name = "secret_data", columnDefinition = "TEXT")
    private String secretData;
    
    @Column(name = "credential_data", columnDefinition = "TEXT")
    private String credentialData;
    
    @Column(name = "priority")
    private Integer priority;

    // Constructors
    public KeycloakCredential() {}

    public KeycloakCredential(String id, String type, String userId, String secretData, String credentialData) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.createdDate = System.currentTimeMillis();
        this.userLabel = "password";
        this.secretData = secretData;
        this.credentialData = credentialData;
        this.priority = 10;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    public String getSecretData() {
        return secretData;
    }

    public void setSecretData(String secretData) {
        this.secretData = secretData;
    }

    public String getCredentialData() {
        return credentialData;
    }

    public void setCredentialData(String credentialData) {
        this.credentialData = credentialData;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
