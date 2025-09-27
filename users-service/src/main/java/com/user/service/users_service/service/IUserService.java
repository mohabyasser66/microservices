package com.user.service.users_service.service;

import com.user.service.users_service.dto.*;
import com.user.service.users_service.model.User;
import com.user.service.users_service.model.Role;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.UUID;

public interface IUserService {

    // ============ BASIC CRUD OPERATIONS ============
    User getUserById(UUID userId);

    User createUser(CreateUserRequest request);

    User updateUser(UpdateUserRequest request, UUID userId);

    void deleteUser(UUID userId);

    Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir, Boolean isActive);

    UserDto convertUserToDto(User user);

    // ============ ACCOUNT MANAGEMENT ============
    void activateUser(UUID userId);

    void deactivateUser(UUID userId);

    void changePassword(UUID userId, ChangePasswordRequest request);

    // ============ EMAIL VERIFICATION ============
    void sendEmailVerification(UUID userId);

    void verifyEmail(String token);

    boolean isEmailVerified(UUID userId);

    // ============ ACCOUNT SECURITY ============

    void resetFailedLoginAttempts(UUID userId);

    void recordFailedLoginAttempt(UUID userId);

    // ============ ROLE MANAGEMENT ============
    void assignRoleToUser(UUID userId, UUID roleId);

    void removeRoleFromUser(UUID userId, UUID roleId);

    Collection<Role> getUserRoles(UUID userId);

    boolean userHasRole(UUID userId, String roleName);

    // ============ SEARCH AND FILTER ============
    Page<UserDto> searchUsers(String email, String firstName, String lastName, String roleName, int page, int size);

    User getUserByEmail(String email);

    Page<UserDto> getUsersByRole(String roleName, int page, int size);

    // ============ STATISTICS AND ANALYTICS ============
    UserStatisticsDto getUserStatistics();

    long getUserCount(Boolean isActive, Boolean isEmailVerified);

    // ============ AUDIT AND HISTORY ============
    Page<LoginHistoryDto> getUserLoginHistory(UUID userId, int page, int size);

    void recordUserLogin(UUID userId);
}
