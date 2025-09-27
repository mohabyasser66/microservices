package com.user.service.users_service.repository;

import com.user.service.users_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // BASIC QUERIES 
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByIsActive(Boolean isActive, Pageable pageable);

    // SEARCH QUERIES
    @Query("SELECT u FROM User u WHERE " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
    Page<User> searchUsers(@Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE LOWER(r.name) = LOWER(:roleName)")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    // STATISTICS QUERIES
    long countByIsActive(Boolean isActive);

    long countByIsEmailVerified(Boolean isEmailVerified);

    long countByIsActiveAndIsEmailVerified(Boolean isActive, Boolean isEmailVerified);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
    long countLockedUsers(@Param("now") LocalDateTime now);

    // ROLE-BASED QUERIES 
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("roleName") String roleName, Pageable pageable);

    // AUDIT QUERIES 
    @Query("SELECT u FROM User u WHERE u.lastLoginAt BETWEEN :startDate AND :endDate")
    Page<User> findUsersWithLoginBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
