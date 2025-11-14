package com.user.service.users_service.repository;

import com.user.service.users_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // BASIC QUERIES 
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    // SEARCH QUERIES
    @Query("SELECT u FROM User u JOIN u.roles r WHERE LOWER(r.name) = LOWER(:roleName)")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    // ROLE-BASED QUERIES 
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("roleName") String roleName, Pageable pageable);

    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

}
