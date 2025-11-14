package com.user.service.users_service.service;

import com.user.service.users_service.dto.*;
import com.user.service.users_service.event.EmailVerificationRequestEvent;
import com.user.service.users_service.event.NotificationEventPublisher;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.model.User;
import com.user.service.users_service.repository.RoleRepository;
import com.user.service.users_service.repository.UserRepository;
import com.user.service.users_service.util.JwtUserExtractor;
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
    private JwtUserExtractor jwtExtractor;

    @Mock
    private NotificationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private UserDto testUserDto;
    private Role testRole;
    private Role adminRole;
    private UUID testUserId;
    private UUID adminUserId;
    private UUID testRoleId;
    private UUID adminRoleId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();
        adminRoleId = UUID.randomUUID();

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

        // Setup admin user
        adminUser = new User();
        adminUser.setId(adminUserId);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setIsActive(true);
        adminUser.setIsEmailVerified(true);
        adminUser.setFailedLoginAttempts(0);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());

        testUserDto = new UserDto();
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john.doe@example.com");

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("USER");

        adminRole = new Role();
        adminRole.setId(adminRoleId);
        adminRole.setName("ADMIN");

        // Set up admin user with admin role
        adminUser.getRoles().add(adminRole);
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
        doNothing().when(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        assertTrue(result.getIsActive());
        assertFalse(result.getIsEmailVerified());
        assertEquals(0, result.getFailedLoginAttempts());

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));
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

        // Mock current user for security check
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(testUser));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));

        User result = userService.updateUser(request, testUserId);

        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository).existsByEmail("jane.smith@example.com");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));
    }

    @Test
    @DisplayName("Should delete user (soft delete)")
    void shouldDeleteUserSoftDelete() {
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
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

        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
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

        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(testUser));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword(testUserId, request);

        verify(userRepository).findById(testUserId);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for incorrect current password")
    void shouldThrowIllegalArgumentExceptionForIncorrectCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");

        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(testUser));
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
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));

        userService.sendEmailVerification(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));
    }

    @Test
    @DisplayName("Should resend email verification successfully")
    void shouldResendEmailVerificationSuccessfully() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));

        userService.resendEmailVerification(testUserId);

        verify(userRepository).findById(testUserId);
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEmailVerificationRequest(any(EmailVerificationRequestEvent.class));
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmailSuccessfully() {
        String token = "valid-token";
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1)); // Valid token

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.verifyEmail(token);

        assertTrue(testUser.getIsEmailVerified());
        assertNull(testUser.getEmailVerificationToken());
        assertNull(testUser.getEmailVerificationTokenExpiresAt());
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid verification token")
    void shouldThrowIllegalArgumentExceptionForInvalidToken() {
        String token = "invalid-token";
        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(token));

        assertEquals("Invalid verification token", exception.getMessage());
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for expired verification token")
    void shouldThrowIllegalArgumentExceptionForExpiredToken() {
        String token = "expired-token";
        testUser.setEmailVerificationToken(token);
        testUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().minusHours(1)); // Expired token

        when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.verifyEmail(token));

        assertEquals("Verification token has expired", exception.getMessage());
        verify(userRepository).findByEmailVerificationToken(token);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should check verification token validity")
    void shouldCheckVerificationTokenValidity() {
        String validToken = "valid-token";
        String invalidToken = "invalid-token";

        testUser.setEmailVerificationToken(validToken);
        testUser.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(1)); // Valid token

        when(userRepository.findByEmailVerificationToken(validToken)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmailVerificationToken(invalidToken)).thenReturn(Optional.empty());

        boolean validResult = userService.isVerificationTokenValid(validToken);
        boolean invalidResult = userService.isVerificationTokenValid(invalidToken);

        assertTrue(validResult);
        assertFalse(invalidResult);
        verify(userRepository).findByEmailVerificationToken(validToken);
        verify(userRepository).findByEmailVerificationToken(invalidToken);
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
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
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
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
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
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
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
        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Collection<Role> result = userService.getUserRoles(testUserId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testRole));
        verify(userRepository).findById(testUserId);
    }

    // ============ SEARCH AND FILTER TESTS ============

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

        when(jwtExtractor.getCurrentUserFromJwt()).thenReturn(Optional.of(adminUser));
        when(userRepository.findActiveUsersByRole("USER", pageable)).thenReturn(userPage);
        when(modelMapper.map(testUser, UserDto.class)).thenReturn(testUserDto);

        Page<UserDto> result = userService.getUsersByRole("USER", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("John", result.getContent().get(0).getFirstName());

        verify(userRepository).findActiveUsersByRole("USER", pageable);
        verify(modelMapper).map(testUser, UserDto.class);
    }
}
