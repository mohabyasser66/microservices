package com.eshop.sync.repository.keycloak;

import com.eshop.sync.entity.keycloak.KeycloakUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Keycloak User entities
 */
@Repository
public interface KeycloakUserRepository extends JpaRepository<KeycloakUser, String> {
    
    Optional<KeycloakUser> findByUsername(String username);
    
    Optional<KeycloakUser> findByEmail(String email);
    
    List<KeycloakUser> findByRealmId(String realmId);
    
    List<KeycloakUser> findByEnabledTrue();
    
    @Query("SELECT u FROM KeycloakUser u WHERE u.realmId = :realmId AND u.enabled = true")
    List<KeycloakUser> findActiveUsersByRealm(@Param("realmId") String realmId);
    
    @Query("SELECT COUNT(u) FROM KeycloakUser u WHERE u.realmId = :realmId AND u.enabled = true")
    long countActiveUsersByRealm(@Param("realmId") String realmId);
}
