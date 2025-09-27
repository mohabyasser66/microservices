package com.eshop.sync.repository.app;

import com.eshop.sync.entity.app.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Application Role entities
 */
@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, String> {
    
    Optional<AppRole> findByName(String name);
}
