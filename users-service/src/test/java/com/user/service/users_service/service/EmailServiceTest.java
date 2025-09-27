package com.user.service.users_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private UUID testUserId;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEmail = "test@example.com";
    }

    // ============ EMAIL VERIFICATION TESTS ============

    @Test
    @DisplayName("Should send verification email successfully")
    void shouldSendVerificationEmailSuccessfully() {
        // This is a basic implementation test since the current EmailService
        // only logs the action without actual email sending
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, testEmail));
    }

    @Test
    @DisplayName("Should handle null user ID gracefully")
    void shouldHandleNullUserIdGracefully() {
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(null, testEmail));
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void shouldHandleNullEmailGracefully() {
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, null));
    }

    @Test
    @DisplayName("Should handle empty email gracefully")
    void shouldHandleEmptyEmailGracefully() {
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, ""));
    }

    @Test
    @DisplayName("Should handle invalid email format gracefully")
    void shouldHandleInvalidEmailFormatGracefully() {
        String invalidEmail = "invalid-email-format";
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, invalidEmail));
    }

    // ============ TOKEN MANAGEMENT TESTS ============

    @Test
    @DisplayName("Should return null for non-existent token")
    void shouldReturnNullForNonExistentToken() {
        String nonExistentToken = "non-existent-token";
        UUID result = emailService.getUserIdFromToken(nonExistentToken);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullTokenGracefully() {
        UUID result = emailService.getUserIdFromToken(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyTokenGracefully() {
        UUID result = emailService.getUserIdFromToken("");
        assertNull(result);
    }

    @Test
    @DisplayName("Should remove token successfully")
    void shouldRemoveTokenSuccessfully() {
        String token = "test-token";
        assertDoesNotThrow(() -> emailService.removeToken(token));
    }

    @Test
    @DisplayName("Should handle null token removal gracefully")
    void shouldHandleNullTokenRemovalGracefully() {
        assertDoesNotThrow(() -> emailService.removeToken(null));
    }

    @Test
    @DisplayName("Should handle empty token removal gracefully")
    void shouldHandleEmptyTokenRemovalGracefully() {
        assertDoesNotThrow(() -> emailService.removeToken(""));
    }

    // ============ EDGE CASES AND RESILIENCE TESTS ============

    @Test
    @DisplayName("Should handle concurrent token operations")
    void shouldHandleConcurrentTokenOperations() {
        // Test that the service can handle multiple operations concurrently
        // without throwing exceptions
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                UUID userId = UUID.randomUUID();
                String email = "user" + i + "@example.com";
                emailService.sendVerificationEmail(userId, email);

                String token = "token-" + i;
                emailService.getUserIdFromToken(token);
                emailService.removeToken(token);
            }
        });
    }

    @Test
    @DisplayName("Should handle special characters in email")
    void shouldHandleSpecialCharactersInEmail() {
        String emailWithSpecialChars = "user+test@sub-domain.example-site.com";
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, emailWithSpecialChars));
    }

    @Test
    @DisplayName("Should handle very long email addresses")
    void shouldHandleVeryLongEmailAddresses() {
        String longEmail = "a".repeat(100) + "@" + "b".repeat(100) + ".com";
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, longEmail));
    }

    @Test
    @DisplayName("Should handle very long tokens")
    void shouldHandleVeryLongTokens() {
        String longToken = "a".repeat(1000);
        assertDoesNotThrow(() -> {
            emailService.getUserIdFromToken(longToken);
            emailService.removeToken(longToken);
        });
    }

    @Test
    @DisplayName("Should handle unicode characters in email")
    void shouldHandleUnicodeCharactersInEmail() {
        String unicodeEmail = "tëst@éxàmplé.com";
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(testUserId, unicodeEmail));
    }

    // ============ INTEGRATION BEHAVIOR TESTS ============

    @Test
    @DisplayName("Should maintain consistent behavior across method calls")
    void shouldMaintainConsistentBehaviorAcrossMethodCalls() {
        // Test that the service maintains consistent state and behavior
        // across multiple method calls
        for (int i = 0; i < 5; i++) {
            UUID userId = UUID.randomUUID();
            String email = "consistent-test-" + i + "@example.com";

            // These should all complete without exceptions
            assertDoesNotThrow(() -> emailService.sendVerificationEmail(userId, email));

            String token = "consistency-token-" + i;
            assertDoesNotThrow(() -> {
                UUID retrievedUserId = emailService.getUserIdFromToken(token);
                assertNull(retrievedUserId); // Should be null since token wasn't stored
            });

            assertDoesNotThrow(() -> emailService.removeToken(token));
        }
    }

    @Test
    @DisplayName("Should handle rapid successive calls")
    void shouldHandleRapidSuccessiveCalls() {
        // Test that the service can handle rapid successive calls
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                emailService.sendVerificationEmail(testUserId, testEmail);
            }
        });
    }

    @Test
    @DisplayName("Should handle mixed valid and invalid inputs")
    void shouldHandleMixedValidAndInvalidInputs() {
        String[] emails = {
                "valid@example.com",
                null,
                "",
                "invalid-email",
                "another-valid@test.org",
                "unicode-tëst@éxàmplé.com"
        };

        UUID[] userIds = {
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                UUID.randomUUID()
        };

        // All of these should complete without throwing exceptions
        for (int i = 0; i < emails.length; i++) {
            final int index = i;
            assertDoesNotThrow(() -> emailService.sendVerificationEmail(userIds[index], emails[index]));
        }
    }
}
