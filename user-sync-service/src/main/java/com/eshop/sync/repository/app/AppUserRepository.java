package com.eshop.sync.repository.app;

import com.eshop.sync.entity.app.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Application User entities
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {
    
    Optional<AppUser> findByUsername(String username);
    
    Optional<AppUser> findByEmail(String email);
    
    List<AppUser> findByUpdatedAtAfter(LocalDateTime timestamp);
    
    List<AppUser> findByEnabledTrue();
    
    @Query("SELECT u FROM AppUser u WHERE u.updatedAt > :timestamp")
    List<AppUser> findModifiedAfter(@Param("timestamp") LocalDateTime timestamp);
    
    @Modifying
    @Query("UPDATE AppUser u SET u.updatedAt = :timestamp WHERE u.id = :id")
    void updateTimestamp(@Param("id") String id, @Param("timestamp") LocalDateTime timestamp);
    
    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.enabled = true")
    long countActiveUsers();
}
