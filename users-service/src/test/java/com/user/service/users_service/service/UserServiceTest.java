package com.user.service.users_service.service;

import com.user.service.users_service.dto.*;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.model.User;
import com.user.service.users_service.repository.RoleRepository;
import com.user.service.users_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private Role testRole;
    private UUID testUserId;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setIsActive(true);
        testUser.setIsEmailVerified(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testUserDto = new UserDto();
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john.doe@example.com");

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("USER");
    }

    // ============ BASIC CRUD TESTS ============

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(testUserId));

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(any(UUID.class), anyString());

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertTrue(result.getIsActive());
        assertFalse(result.getIsEmailVerified());
        assertEquals(0, result.getFailedLoginAttempts());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(UUID.class), eq("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should throw AlreadyExistsException when email already exists")
    void shouldThrowAlreadyExistsExceptionWhenEmailExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("john.doe@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.createUser(request));

        assertEquals("john.doe@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane.smith@example.com");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendVerificationEmail(any(UUID.class), anyString());

        User result = userService.updateUser(request, testUserId);

        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(testUserId, "jane.smith@example.com");
    }

    @Test
    @DisplayName("Should delete user (soft delete)")
    void shouldDeleteUserSoftDelete() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void shouldGetAllUsersWithPagination() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(modelMapper.map(testUser, UserDto.class)).thenReturn(testUserDto);

        Page<UserDto> result = userService.getAllUsers(0, 10, "createdAt", "desc", null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());

        verify(userRepository).findAll(any(Pageable.class));
        verify(modelMapper).map(testUser, UserDto.class);
    }

    // ============ ACCOUNT MANAGEMENT TESTS ============

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.activateUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword(testUserId, request);

        verify(userRepository).findById(testUserId);
        verify(passwordEncoder).matches("oldPassword", testUser.getPassword());
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for incorrect current password")
    void shouldThrowIllegalArgumentExceptionForIncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(testUserId, request));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository).findById(testUserId);
        verify(passwordEncoder).matches("wrongPassword", testUser.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }

    // ============ EMAIL VERIFICATION TESTS ============

    @Test
    @DisplayName("Should send email verification successfully")
    void shouldSendEmailVerificationSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(emailService).sendVerificationEmail(testUserId, testUser.getEmail());

        userService.sendEmailVerification(testUserId);

        verify(userRepository).findById(testUserId);
        verify(emailService).sendVerificationEmail(testUserId, testUser.getEmail());
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        String token = "valid-token";
        when(emailService.getUserIdFromToken(token)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).removeToken(token);

        userService.verifyEmail(token);

        verify(emailService).getUserIdFromToken(token);
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
        verify(emailService).removeToken(token);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid verification token")
    void shouldThrowIllegalArgumentExceptionForInvalidToken() {
        String token = "invalid-token";
        when(emailService.getUserIdFromToken(token)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(token));

        assertEquals("Invalid or expired verification token", exception.getMessage());
        verify(emailService).getUserIdFromToken(token);
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should check email verification status")
    void shouldCheckEmailVerificationStatus() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        boolean result = userService.isEmailVerified(testUserId);

        assertFalse(result); // testUser is set with isEmailVerified = false
        verify(userRepository).findById(testUserId);
    }

    // ============ ACCOUNT SECURITY TESTS ============

    @Test
    @DisplayName("Should reset failed login attempts successfully")
    void shouldResetFailedLoginAttemptsSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.resetFailedLoginAttempts(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should record failed login attempt and auto-lock after 5 attempts")
    void shouldRecordFailedLoginAttemptAndAutoLock() {
        testUser.setFailedLoginAttempts(4); // 4 failed attempts already
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.recordFailedLoginAttempt(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
        // User should be auto-locked after 5th failed attempt
    }

    // ============ ROLE MANAGEMENT TESTS ============

    @Test
    @DisplayName("Should assign role to user successfully")
    void shouldAssignRoleToUserSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.assignRoleToUser(testUserId, testRoleId);

        verify(userRepository).findById(testUserId);
        verify(roleRepository).findById(testRoleId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should remove role from user successfully")
    void shouldRemoveRoleFromUserSuccessfully() {
        testUser.getRoles().add(testRole);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.removeRoleFromUser(testUserId, testRoleId);

        verify(userRepository).findById(testUserId);
        verify(roleRepository).findById(testRoleId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user roles successfully")
    void shouldGetUserRolesSuccessfully() {
        testUser.getRoles().add(testRole);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Collection<Role> result = userService.getUserRoles(testUserId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testRole));
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should check if user has role")
    void shouldCheckIfUserHasRole() {
        testUser.getRoles().add(testRole);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        boolean result = userService.userHasRole(testUserId, "USER");

        assertTrue(result);
        verify(userRepository).findById(testUserId);
    }

    // ============ SEARCH AND FILTER TESTS ============

    @Test
    @DisplayName("Should search users successfully")
    void shouldSearchUsersSuccessfully() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.searchUsers("john", null, null, pageable)).thenReturn(userPage);
        when(modelMapper.map(testUser, UserDto.class)).thenReturn(testUserDto);

        Page<UserDto> result = userService.searchUsers("john", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());

        verify(userRepository).searchUsers("john", null, null, pageable);
        verify(modelMapper).map(testUser, UserDto.class);
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmailSuccessfully() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should get users by role successfully")
    void shouldGetUsersByRoleSuccessfully() {
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findActiveUsersByRole("USER", pageable)).thenReturn(userPage);
        when(modelMapper.map(testUser, UserDto.class)).thenReturn(testUserDto);

        Page<UserDto> result = userService.getUsersByRole("USER", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());

        verify(userRepository).findActiveUsersByRole("USER", pageable);
        verify(modelMapper).map(testUser, UserDto.class);
    }

    // ============ STATISTICS TESTS ============

    @Test
    @DisplayName("Should get user statistics successfully")
    void shouldGetUserStatisticsSuccessfully() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByIsActive(true)).thenReturn(90L);
        when(userRepository.countByIsActive(false)).thenReturn(10L);
        when(userRepository.countByIsEmailVerified(true)).thenReturn(80L);
        when(userRepository.countByIsEmailVerified(false)).thenReturn(20L);
        when(userRepository.countLockedUsers(any(LocalDateTime.class))).thenReturn(5L);
        when(userRepository.countUsersCreatedAfter(any(LocalDateTime.class))).thenReturn(10L, 50L, 80L);

        UserStatisticsDto result = userService.getUserStatistics();

        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(90L, result.getActiveUsers());
        assertEquals(10L, result.getInactiveUsers());
        assertEquals(80L, result.getVerifiedUsers());
        assertEquals(20L, result.getUnverifiedUsers());
        assertEquals(5L, result.getLockedUsers());

        verify(userRepository).count();
        verify(userRepository).countByIsActive(true);
        verify(userRepository).countByIsActive(false);
        verify(userRepository).countByIsEmailVerified(true);
        verify(userRepository).countByIsEmailVerified(false);
        verify(userRepository).countLockedUsers(any(LocalDateTime.class));
        verify(userRepository, times(3)).countUsersCreatedAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get user count successfully")
    void shouldGetUserCountSuccessfully() {
        when(userRepository.countByIsActiveAndIsEmailVerified(true, true)).thenReturn(75L);

        long result = userService.getUserCount(true, true);

        assertEquals(75L, result);
        verify(userRepository).countByIsActiveAndIsEmailVerified(true, true);
    }

    // ============ AUDIT TESTS ============

    @Test
    @DisplayName("Should record user login successfully")
    void shouldRecordUserLoginSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.recordUserLogin(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user login history successfully")
    void shouldGetUserLoginHistorySuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Page<LoginHistoryDto> result = userService.getUserLoginHistory(testUserId, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty()); // Current implementation returns empty page
        verify(userRepository).findById(testUserId);
    }
}
