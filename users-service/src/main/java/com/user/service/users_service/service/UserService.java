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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.user.service.users_service.util.JwtUserExtractor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUserExtractor jwtExtractor;
    private final NotificationEventPublisher eventPublisher;

    // BASIC CRUD OPERATIONS

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return targetUser;
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setId(UUID.randomUUID());
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setIsActive(true);
                    user.setIsEmailVerified(false);
                    user.setFailedLoginAttempts(0);

                    user.generateEmailVerificationToken();

                    User savedUser = userRepository.save(user);

                    EmailVerificationRequestEvent event = EmailVerificationRequestEvent.builder()
                            .userId(savedUser.getId())
                            .email(savedUser.getEmail())
                            .firstName(savedUser.getFirstName())
                            .lastName(savedUser.getLastName())
                            .verificationToken(savedUser.getEmailVerificationToken())
                            .verificationUrl(
                                    "http://localhost:3000/verify-email?token=" + savedUser.getEmailVerificationToken())
                            .build();

                    eventPublisher.publishEmailVerificationRequest(event);

                    log.info("User created successfully with ID: {}", savedUser.getId());
                    return savedUser;
                }).orElseThrow(() -> new AlreadyExistsException(request.getEmail() + " already exists"));
    }

    @Override
    public User updateUser(UpdateUserRequest request, UUID userId) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt().get();
        if (!currentUser.getId().equals(userId) && !currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Can't update other user's profile");
        }
        return userRepository.findById(userId).map(existingUser -> {
            if (request.getFirstName() != null) {
                existingUser.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                existingUser.setLastName(request.getLastName());
            }
            if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new AlreadyExistsException("Email " + request.getEmail() + " already exists");
                }
                existingUser.setEmail(request.getEmail());
                existingUser.setIsEmailVerified(false);

                existingUser.generateEmailVerificationToken();

                EmailVerificationRequestEvent event = EmailVerificationRequestEvent.builder()
                        .userId(existingUser.getId())
                        .email(existingUser.getEmail())
                        .firstName(existingUser.getFirstName())
                        .lastName(existingUser.getLastName())
                        .verificationToken(existingUser.getEmailVerificationToken())
                        .verificationUrl(
                                "http://localhost:3000/verify-email?token=" + existingUser.getEmailVerificationToken())
                        .build();

                eventPublisher.publishEmailVerificationRequest(event);
            }

            User updatedUser = userRepository.save(existingUser);
            log.info("User updated successfully with ID: {}", updatedUser.getId());
            return updatedUser;
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public void deleteUser(UUID userId) {
        Optional<User> currentUser = jwtExtractor.getCurrentUserFromJwt();
        if (!currentUser.isPresent() || !currentUser.get().hasRole("ADMIN")) {
            throw new SecurityException("Only admins can delete users");
        }
        if (currentUser.get().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        userRepository.findById(userId).ifPresentOrElse(user -> {
            // Deactivated instead of deletion
            user.setIsActive(false);
            userRepository.save(user);
            log.info("User deactivated with ID: {}", userId);
        }, () -> {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(int page, int size, String sortBy, String sortDir, Boolean isActive) {
        Optional<User> currentUser = jwtExtractor.getCurrentUserFromJwt();
        if (!currentUser.isPresent() || !currentUser.get().hasRole("ADMIN")) {
            throw new SecurityException("Only admins can view all users");
        }
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users;
        if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::convertUserToDto);
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // ACCOUNT MANAGEMENT

    @Override
    public void activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        userRepository.save(user);
        log.info("User activated with ID: {}", userId);
    }

    @Override
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated with ID: {}", userId);
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.getId().equals(userId) && !currentUser.hasRole("ADMIN")) {
            throw new SecurityException("You can only change your own password");
        }

        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user ID: {}", userId);
    }

    // EMAIL VERIFICATION

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (!user.isEmailVerificationTokenValid()) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setIsEmailVerified(true);
        user.clearEmailVerificationToken();
        userRepository.save(user);

        log.info("Email verified for user ID: {}", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailVerified(UUID userId) {
        User user = getUserById(userId);
        return user.getIsEmailVerified();
    }

    // ACCOUNT SECURITY

    @Override
    public void resetFailedLoginAttempts(UUID userId) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Only admins can reset failed login attempts");
        }
        User user = getUserById(userId);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Failed login attempts reset for user ID: {}", userId);
    }

    @Override
    public void recordFailedLoginAttempt(UUID userId) {
        User user = getUserById(userId);
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("User auto-locked due to 5 failed login attempts, User ID: {}", userId);
        }
        userRepository.save(user);
    }

    // ROLE MANAGEMENT

    @Override
    public void assignRoleToUser(UUID userId, UUID roleId) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Only admins can assign roles to users");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Role {} assigned to user ID: {}", role.getName(), userId);
    }

    @Override
    public void removeRoleFromUser(UUID userId, UUID roleId) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Only admins can remove roles from users");
        }
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.getRoles().remove(role);
        userRepository.save(user);
        log.info("Role {} removed from user ID: {}", role.getName(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Role> getUserRoles(UUID userId) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Admin can only view roles");
        }
        User user = getUserById(userId);
        return user.getRoles();
    }

    // SEARCH AND FILTER

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(String roleName, int page, int size) {
        User currentUser = jwtExtractor.getCurrentUserFromJwt()
                .orElseThrow(() -> new SecurityException("User not found"));
        if (!currentUser.hasRole("ADMIN")) {
            throw new SecurityException("Only admins can view users by role");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findActiveUsersByRole(roleName, pageable);
        return users.map(this::convertUserToDto);
    }

    // NEW EMAIL VERIFICATION METHODS

    @Override
    public void sendEmailVerification(UUID userId) {
        User user = getUserById(userId);

        if (user.getIsEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate new token
        user.generateEmailVerificationToken();
        userRepository.save(user);

        // Publish event to notification service
        EmailVerificationRequestEvent event = EmailVerificationRequestEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .verificationToken(user.getEmailVerificationToken())
                .verificationUrl("http://localhost:3000/verify-email?token=" + user.getEmailVerificationToken())
                .build();

        eventPublisher.publishEmailVerificationRequest(event);

        log.info("Email verification sent for user ID: {}", userId);
    }

    @Override
    public void resendEmailVerification(UUID userId) {
        User user = getUserById(userId);

        if (user.getIsEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        // Generate new token
        user.generateEmailVerificationToken();
        userRepository.save(user);

        // Publish event to notification service
        EmailVerificationRequestEvent event = EmailVerificationRequestEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .verificationToken(user.getEmailVerificationToken())
                .verificationUrl("http://localhost:3000/verify-email?token=" + user.getEmailVerificationToken())
                .build();

        eventPublisher.publishEmailVerificationRequest(event);

        log.info("Email verification resent for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVerificationTokenValid(String token) {
        return userRepository.findByEmailVerificationToken(token)
                .map(User::isEmailVerificationTokenValid)
                .orElse(false);
    }

}
