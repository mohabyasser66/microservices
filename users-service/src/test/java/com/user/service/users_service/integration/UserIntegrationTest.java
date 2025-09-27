package com.user.service.users_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.service.users_service.dto.CreateUserRequest;
import com.user.service.users_service.dto.UpdateUserRequest;
import com.user.service.users_service.dto.ChangePasswordRequest;
import com.user.service.users_service.model.User;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.repository.UserRepository;
import com.user.service.users_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Create test role
        testRole = new Role("USER");
        testRole = roleRepository.save(testRole);

        // Create test user
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);
        testUser.setFailedLoginAttempts(0);
        testUser = userRepository.save(testUser);
    }

    // ============ USER CRUD INTEGRATION TESTS ============

    @Test
    @DisplayName("Should create user via REST API")
    void shouldCreateUserViaRestApi() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")))
                .andExpect(jsonPath("$.isActive", is(true)))
                .andExpect(jsonPath("$.isEmailVerified", is(false)));
    }

    @Test
    @DisplayName("Should get user by ID via REST API")
    void shouldGetUserByIdViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    @DisplayName("Should update user via REST API")
    void shouldUpdateUserViaRestApi() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John Updated");
        request.setLastName("Doe Updated");
        request.setEmail("john.updated@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John Updated")))
                .andExpect(jsonPath("$.lastName", is("Doe Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")));
    }

    @Test
    @DisplayName("Should delete user via REST API")
    void shouldDeleteUserViaRestApi() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is soft deleted (isActive = false)
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    @DisplayName("Should get all users with pagination via REST API")
    void shouldGetAllUsersWithPaginationViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].firstName", is("John")));
    }

    // ============ ACCOUNT MANAGEMENT INTEGRATION TESTS ============

    @Test
    @DisplayName("Should activate user via REST API")
    void shouldActivateUserViaRestApi() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/activate", testUser.getId()))
                .andExpect(status().isOk());

        // Verify user is activated
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    @DisplayName("Should deactivate user via REST API")
    void shouldDeactivateUserViaRestApi() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/deactivate", testUser.getId()))
                .andExpect(status().isOk());

        // Verify user is deactivated
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive", is(false)));
    }

    @Test
    @DisplayName("Should change password via REST API")
    void shouldChangePasswordViaRestApi() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("encodedPassword");
        request.setNewPassword("newPassword123");

        mockMvc.perform(patch("/api/users/{id}/change-password", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ============ EMAIL VERIFICATION INTEGRATION TESTS ============

    @Test
    @DisplayName("Should send email verification via REST API")
    void shouldSendEmailVerificationViaRestApi() throws Exception {
        mockMvc.perform(post("/api/users/{id}/send-verification", testUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should check email verification status via REST API")
    void shouldCheckEmailVerificationStatusViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/{id}/email-verified", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false))); // testUser has isEmailVerified = false
    }

    // ============ ROLE MANAGEMENT INTEGRATION TESTS ============

    @Test
    @DisplayName("Should assign role to user via REST API")
    void shouldAssignRoleToUserViaRestApi() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/roles/{roleId}", testUser.getId(), testRole.getId()))
                .andExpect(status().isOk());

        // Verify role is assigned
        mockMvc.perform(get("/api/users/{id}/roles", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("USER")));
    }

    @Test
    @DisplayName("Should remove role from user via REST API")
    void shouldRemoveRoleFromUserViaRestApi() throws Exception {
        // First assign the role
        mockMvc.perform(post("/api/users/{userId}/roles/{roleId}", testUser.getId(), testRole.getId()))
                .andExpect(status().isOk());

        // Then remove it
        mockMvc.perform(delete("/api/users/{userId}/roles/{roleId}", testUser.getId(), testRole.getId()))
                .andExpect(status().isOk());

        // Verify role is removed
        mockMvc.perform(get("/api/users/{id}/roles", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should get user roles via REST API")
    void shouldGetUserRolesViaRestApi() throws Exception {
        // Assign role first
        testUser.getRoles().add(testRole);
        testUser = userRepository.save(testUser);

        mockMvc.perform(get("/api/users/{id}/roles", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("USER")));
    }

    @Test
    @DisplayName("Should check if user has role via REST API")
    void shouldCheckIfUserHasRoleViaRestApi() throws Exception {
        // Assign role first
        testUser.getRoles().add(testRole);
        testUser = userRepository.save(testUser);

        mockMvc.perform(get("/api/users/{id}/has-role/{roleName}", testUser.getId(), "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));

        mockMvc.perform(get("/api/users/{id}/has-role/{roleName}", testUser.getId(), "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));
    }

    // ============ SEARCH AND FILTER INTEGRATION TESTS ============

    @Test
    @DisplayName("Should search users via REST API")
    void shouldSearchUsersViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/search")
                .param("query", "John")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].firstName", containsString("John")));
    }

    @Test
    @DisplayName("Should get user by email via REST API")
    void shouldGetUserByEmailViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));
    }

    @Test
    @DisplayName("Should get users by role via REST API")
    void shouldGetUsersByRoleViaRestApi() throws Exception {
        // Assign role to user first
        testUser.getRoles().add(testRole);
        testUser = userRepository.save(testUser);

        mockMvc.perform(get("/api/users/role/{roleName}", "USER")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].firstName", is("John")));
    }

    // ============ STATISTICS INTEGRATION TESTS ============

    @Test
    @DisplayName("Should get user statistics via REST API")
    void shouldGetUserStatisticsViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeUsers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.inactiveUsers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.verifiedUsers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.unverifiedUsers", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should get user count via REST API")
    void shouldGetUserCountViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/count")
                .param("isActive", "true")
                .param("isEmailVerified", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string(greaterThanOrEqualTo("0")));
    }

    // ============ ERROR HANDLING INTEGRATION TESTS ============

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void shouldReturn404ForNonExistentUser() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 for invalid user creation")
    void shouldReturn400ForInvalidUserCreation() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest();
        // Missing required fields

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 for duplicate email")
    void shouldReturn409ForDuplicateEmail() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail(testUser.getEmail()); // Same email as existing user
        request.setPassword("password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return 400 for invalid UUID format")
    void shouldReturn400ForInvalidUuidFormat() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    // ============ SECURITY INTEGRATION TESTS ============

    @Test
    @DisplayName("Should lock user via REST API")
    void shouldLockUserViaRestApi() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/lock", testUser.getId())
                .param("durationMinutes", "30"))
                .andExpect(status().isOk());

        // Verify user is locked
        mockMvc.perform(get("/api/users/{id}/locked", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    @DisplayName("Should unlock user via REST API")
    void shouldUnlockUserViaRestApi() throws Exception {
        // First lock the user
        mockMvc.perform(patch("/api/users/{id}/lock", testUser.getId())
                .param("durationMinutes", "30"))
                .andExpect(status().isOk());

        // Then unlock
        mockMvc.perform(patch("/api/users/{id}/unlock", testUser.getId()))
                .andExpect(status().isOk());

        // Verify user is unlocked
        mockMvc.perform(get("/api/users/{id}/locked", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));
    }

    @Test
    @DisplayName("Should reset failed login attempts via REST API")
    void shouldResetFailedLoginAttemptsViaRestApi() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/reset-failed-attempts", testUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should record failed login attempt via REST API")
    void shouldRecordFailedLoginAttemptViaRestApi() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/failed-attempt", testUser.getId()))
                .andExpect(status().isOk());
    }

    // ============ AUDIT INTEGRATION TESTS ============

    @Test
    @DisplayName("Should record user login via REST API")
    void shouldRecordUserLoginViaRestApi() throws Exception {
        mockMvc.perform(post("/api/users/{id}/login", testUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get user login history via REST API")
    void shouldGetUserLoginHistoryViaRestApi() throws Exception {
        mockMvc.perform(get("/api/users/{id}/login-history", testUser.getId())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", isA(java.util.List.class)));
    }
}
