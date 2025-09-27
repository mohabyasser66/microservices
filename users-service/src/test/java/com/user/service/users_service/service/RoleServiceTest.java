package com.user.service.users_service.service;

import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.model.User;
import com.user.service.users_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testRole = new Role("USER");
        testRole.setId(testRoleId);
    }

    // ============ BASIC CRUD TESTS ============

    @Test
    @DisplayName("Should create role successfully")
    void shouldCreateRoleSuccessfully() {
        String roleName = "ADMIN";
        Role newRole = new Role(roleName);

        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        Role result = roleService.createRole(roleName);

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());

        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw AlreadyExistsException when role name already exists")
    void shouldThrowAlreadyExistsExceptionWhenRoleNameExists() {
        String roleName = "USER";

        when(roleRepository.existsByName("USER")).thenReturn(true);

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> roleService.createRole(roleName));

        assertEquals("Role USER already exists", exception.getMessage());
        verify(roleRepository).existsByName("USER");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should get role by ID successfully")
    void shouldGetRoleByIdSuccessfully() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        Role result = roleService.getRoleById(testRoleId);

        assertNotNull(result);
        assertEquals(testRoleId, result.getId());
        assertEquals("USER", result.getName());
        verify(roleRepository).findById(testRoleId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when role not found by ID")
    void shouldThrowResourceNotFoundExceptionWhenRoleNotFoundById() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRoleById(testRoleId));

        assertEquals("Role not found with ID: " + testRoleId, exception.getMessage());
        verify(roleRepository).findById(testRoleId);
    }

    @Test
    @DisplayName("Should get role by name successfully")
    void shouldGetRoleByNameSuccessfully() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));

        Role result = roleService.getRoleByName("USER");

        assertNotNull(result);
        assertEquals("USER", result.getName());
        verify(roleRepository).findByName("USER");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when role not found by name")
    void shouldThrowResourceNotFoundExceptionWhenRoleNotFoundByName() {
        when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getRoleByName("NONEXISTENT"));

        assertEquals("Role not found with name: NONEXISTENT", exception.getMessage());
        verify(roleRepository).findByName("NONEXISTENT");
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void shouldGetAllRolesSuccessfully() {
        List<Role> roles = Arrays.asList(testRole);

        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0).getName());

        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() {
        String newName = "UPDATED_USER";

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("UPDATED_USER")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.updateRole(testRoleId, newName);

        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).existsByName("UPDATED_USER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should throw AlreadyExistsException when updating role with existing name")
    void shouldThrowAlreadyExistsExceptionWhenUpdatingWithExistingName() {
        String newName = "ADMIN";

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("ADMIN")).thenReturn(true);

        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> roleService.updateRole(testRoleId, newName));

        assertEquals("Role ADMIN already exists", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Should not update role when name is same")
    void shouldNotUpdateRoleWhenNameIsSame() {
        String sameName = "USER";

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.updateRole(testRoleId, sameName);

        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository, never()).existsByName(anyString());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should delete role successfully when no users assigned")
    void shouldDeleteRoleSuccessfullyWhenNoUsersAssigned() {
        testRole.setUsers(new HashSet<>());
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        doNothing().when(roleRepository).delete(testRole);

        assertDoesNotThrow(() -> roleService.deleteRole(testRoleId));

        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).delete(testRole);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to delete role with assigned users")
    void shouldThrowIllegalStateExceptionWhenDeletingRoleWithUsers() {
        User assignedUser = new User();
        testRole.getUsers().add(assignedUser);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> roleService.deleteRole(testRoleId));

        assertEquals("Cannot delete role USER as it has users assigned", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository, never()).delete(any(Role.class));
    }

    // ============ EDGE CASES AND ERROR HANDLING ============

    @Test
    @DisplayName("Should handle null role name in create")
    void shouldHandleNullRoleNameInCreate() {
        // The Role constructor handles null by setting name to null
        // The service should validate and fail appropriately
        when(roleRepository.existsByName(null)).thenThrow(new RuntimeException("Cannot check existence for null name"));

        assertThrows(RuntimeException.class, () -> roleService.createRole(null));
    }

    @Test
    @DisplayName("Should handle empty role name in create")
    void shouldHandleEmptyRoleNameInCreate() {
        String emptyName = "";
        when(roleRepository.existsByName("")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role(emptyName));

        Role result = roleService.createRole(emptyName);

        assertNotNull(result);
        verify(roleRepository).existsByName("");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should convert role name to uppercase")
    void shouldConvertRoleNameToUppercase() {
        String lowerCaseName = "admin";
        when(roleRepository.existsByName("ADMIN")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role("ADMIN"));

        Role result = roleService.createRole(lowerCaseName);

        assertNotNull(result);
        verify(roleRepository).existsByName("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should handle null role name in getRoleByName")
    void shouldHandleNullRoleNameInGetRoleByName() {
        assertThrows(NullPointerException.class, () -> roleService.getRoleByName(null));
    }

    @Test
    @DisplayName("Should handle null role name in update")
    void shouldHandleNullRoleNameInUpdate() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.updateRole(testRoleId, null);

        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository, never()).existsByName(anyString());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Should handle case insensitive name lookup")
    void shouldHandleCaseInsensitiveNameLookup() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(testRole));

        Role result = roleService.getRoleByName("user");

        assertNotNull(result);
        assertEquals("USER", result.getName());
        verify(roleRepository).findByName("USER");
    }

    @Test
    @DisplayName("Should handle case insensitive name update")
    void shouldHandleCaseInsensitiveNameUpdate() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName("MODERATOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.updateRole(testRoleId, "moderator");

        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).existsByName("MODERATOR");
        verify(roleRepository).save(any(Role.class));
    }
}
