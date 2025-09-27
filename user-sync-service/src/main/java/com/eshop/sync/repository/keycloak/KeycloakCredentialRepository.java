package com.eshop.sync.repository.keycloak;

import com.eshop.sync.entity.keycloak.KeycloakCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Keycloak Credential entities
 */
@Repository
public interface KeycloakCredentialRepository extends JpaRepository<KeycloakCredential, String> {
    
    List<KeycloakCredential> findByUserId(String userId);
    
    List<KeycloakCredential> findByUserIdAndType(String userId, String type);
    
    @Query("SELECT c FROM KeycloakCredential c WHERE c.userId = :userId AND c.type = 'password'")
    Optional<KeycloakCredential> findPasswordCredentialByUserId(@Param("userId") String userId);
    
    void deleteByUserId(String userId);
    
    void deleteByUserIdAndType(String userId, String type);
}
