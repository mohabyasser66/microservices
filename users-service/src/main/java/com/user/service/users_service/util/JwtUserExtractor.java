package com.user.service.users_service.util;

import com.user.service.users_service.model.User;
import com.user.service.users_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JWT Utility class for extracting user information from JWT tokens
 * This replaces the deleted KeycloakUserService with static utility methods
 */
@Component
public class JwtUserExtractor {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserExtractor.class);

    private final UserRepository userRepository;

    public JwtUserExtractor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extract the current user from JWT token in SecurityContext
     */
    public Optional<User> getCurrentUserFromJwt() {
        try {
            Jwt jwt = getCurrentJwt();
            if (jwt != null) {
                return getUserFromJwtClaims(jwt);
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to extract user from JWT", e);
            return Optional.empty();
        }
    }

    /**
     * Get the current JWT token from SecurityContext
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken();
        }

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }

        return null;
    }

    /**
     * Extract user information from JWT claims and find in database
     */
    public Optional<User> getUserFromJwtClaims(Jwt jwt) {
        // Try email first (most reliable)
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                logger.debug("Found user by email from JWT: {}", email);
                return user;
            }
        }

        // Try preferred_username
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null) {
            Optional<User> user = userRepository.findByEmail(preferredUsername);
            if (user.isPresent()) {
                logger.debug("Found user by preferred_username from JWT: {}", preferredUsername);
                return user;
            }
        }

        // Try username claim
        String username = jwt.getClaimAsString("username");
        if (username != null) {
            Optional<User> user = userRepository.findByEmail(username);
            if (user.isPresent()) {
                logger.debug("Found user by username from JWT: {}", username);
                return user;
            }
        }

        logger.warn("No user found for JWT claims - email: {}, preferred_username: {}, username: {}",
                email, preferredUsername, username);
        return Optional.empty();
    }

    /**
     * Extract a specific claim from current JWT token
     */
    public Optional<String> getClaimFromCurrentJwt(String claimName) {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            return Optional.ofNullable(jwt.getClaimAsString(claimName));
        }
        return Optional.empty();
    }

    /**
     * Get Keycloak user ID from JWT token
     */
    public Optional<String> getCurrentKeycloakUserId() {
        return getClaimFromCurrentJwt("sub");
    }

    /**
     * Get user email from JWT token
     */
    public Optional<String> getCurrentUserEmail() {
        return getClaimFromCurrentJwt("email");
    }

    /**
     * Get preferred username from JWT token
     */
    public Optional<String> getCurrentUsername() {
        return getClaimFromCurrentJwt("preferred_username");
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(authority -> authority.equals("ROLE_" + role.toUpperCase()));
        }

        return false;
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user can access specific user data
     * (either it's their own data or they are admin)
     */
    public boolean canAccessUserData(String userEmail) {
        if (isAdmin()) {
            return true;
        }

        Optional<String> currentUserEmail = getCurrentUserEmail();
        return currentUserEmail.map(email -> email.equals(userEmail)).orElse(false);
    }

    /**
     * Get all JWT claims as a map for debugging
     */
    public Optional<java.util.Map<String, Object>> getAllClaims() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            return Optional.of(jwt.getClaims());
        }
        return Optional.empty();
    }
}
