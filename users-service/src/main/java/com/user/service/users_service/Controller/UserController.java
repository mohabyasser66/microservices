package com.user.service.users_service.Controller;

import com.user.service.users_service.dto.*;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.User;
import com.user.service.users_service.service.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable UUID userId) {
        try {
            User user = userService.getUserById(userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("Success", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive) {
        try {
            Page<UserDto> users = userService.getAllUsers(page, size, sortBy, sortDir, isActive);
            return ResponseEntity.ok(new ApiResponse("Success", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error retrieving users", null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("User created successfully", userDto));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(
            @Valid @RequestBody UpdateUserRequest request,
            @PathVariable UUID userId) {
        try {
            User user = userService.updateUser(request, userId);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============ ACCOUNT MANAGEMENT ============

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable UUID userId) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(new ApiResponse("User activated successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable UUID userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(new ApiResponse("User deactivated successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PatchMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok(new ApiResponse("Password changed successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============ EMAIL VERIFICATION ============

    @PostMapping("/{userId}/send-verification-email")
    public ResponseEntity<ApiResponse> sendVerificationEmail(@PathVariable UUID userId) {
        try {
            userService.sendEmailVerification(userId);
            return ResponseEntity.ok(new ApiResponse("Verification email sent successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            userService.verifyEmail(request.getToken());
            return ResponseEntity.ok(new ApiResponse("Email verified successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{userId}/email-verification-status")
    public ResponseEntity<ApiResponse> getEmailVerificationStatus(@PathVariable UUID userId) {
        try {
            boolean isVerified = userService.isEmailVerified(userId);
            return ResponseEntity.ok(new ApiResponse("Email verification status retrieved", isVerified));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============ ACCOUNT SECURITY ============

    @PostMapping("/{userId}/reset-failed-attempts")
    public ResponseEntity<ApiResponse> resetFailedLoginAttempts(@PathVariable UUID userId) {
        try {
            userService.resetFailedLoginAttempts(userId);
            return ResponseEntity.ok(new ApiResponse("Failed login attempts reset successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============ ROLE MANAGEMENT ============

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse> assignRoleToUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        try {
            userService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok(new ApiResponse("Role assigned successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse> removeRoleFromUser(
            @PathVariable UUID userId,
            @PathVariable UUID roleId) {
        try {
            userService.removeRoleFromUser(userId, roleId);
            return ResponseEntity.ok(new ApiResponse("Role removed successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse> getUserRoles(@PathVariable UUID userId) {
        try {
            var roles = userService.getUserRoles(userId);
            return ResponseEntity.ok(new ApiResponse("User roles retrieved successfully", roles));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    // ============ SEARCH AND FILTER ============

    @GetMapping("/by-email/{email}")
    public ResponseEntity<ApiResponse> getUserByEmail(@PathVariable String email) {
        try {
            User user = userService.getUserByEmail(email);
            UserDto userDto = userService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("User found", userDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-role/{roleName}")
    public ResponseEntity<ApiResponse> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserDto> users = userService.getUsersByRole(roleName, page, size);
            return ResponseEntity.ok(new ApiResponse("Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error retrieving users", null));
        }
    }

    // ============ EMAIL VERIFICATION ============

    @PostMapping("/{userId}/send-verification")
    public ResponseEntity<ApiResponse> sendEmailVerification(@PathVariable UUID userId) {
        try {
            userService.sendEmailVerification(userId);
            return ResponseEntity.ok(new ApiResponse("Verification email sent successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/{userId}/resend-verification")
    public ResponseEntity<ApiResponse> resendEmailVerification(@PathVariable UUID userId) {
        try {
            userService.resendEmailVerification(userId);
            return ResponseEntity.ok(new ApiResponse("Verification email resent successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(new ApiResponse("Email verified successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/verify-token/{token}")
    public ResponseEntity<ApiResponse> isVerificationTokenValid(@PathVariable String token) {
        try {
            boolean isValid = userService.isVerificationTokenValid(token);
            return ResponseEntity.ok(new ApiResponse("Token validation completed", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error validating token", false));
        }
    }

}
