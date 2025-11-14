package com.user.service.users_service.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.service.users_service.dto.CreateRoleRequest;
import com.user.service.users_service.dto.UpdateRoleRequest;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@DisplayName("RoleController Unit Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private Role testRole;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("USER");
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void shouldGetAllRolesSuccessfully() throws Exception {
        List<Role> roles = Arrays.asList(testRole);
        when(roleService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data[0].name").value("USER"));

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("Should get role by ID successfully")
    void shouldGetRoleByIdSuccessfully() throws Exception {
        when(roleService.getRoleById(testRoleId)).thenReturn(testRole);

        mockMvc.perform(get("/api/roles/{roleId}", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("USER"));

        verify(roleService).getRoleById(testRoleId);
    }

    @Test
    @DisplayName("Should return 404 when role not found by ID")
    void shouldReturn404WhenRoleNotFoundById() throws Exception {
        when(roleService.getRoleById(testRoleId))
                .thenThrow(new ResourceNotFoundException("Role not found"));

        mockMvc.perform(get("/api/roles/{roleId}", testRoleId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found"));

        verify(roleService).getRoleById(testRoleId);
    }

    @Test
    @DisplayName("Should get role by name successfully")
    void shouldGetRoleByNameSuccessfully() throws Exception {
        when(roleService.getRoleByName("USER")).thenReturn(testRole);

        mockMvc.perform(get("/api/roles/by-name/{roleName}", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("USER"));

        verify(roleService).getRoleByName("USER");
    }

    @Test
    @DisplayName("Should create role successfully")
    void shouldCreateRoleSuccessfully() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("ADMIN");

        when(roleService.createRole("ADMIN")).thenReturn(testRole);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Role created successfully"));

        verify(roleService).createRole("ADMIN");
    }

    @Test
    @DisplayName("Should return 409 when role already exists")
    void shouldReturn409WhenRoleAlreadyExists() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("USER");

        when(roleService.createRole("USER"))
                .thenThrow(new AlreadyExistsException("Role USER already exists"));

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Role USER already exists"));

        verify(roleService).createRole("USER");
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("SUPER_USER");

        when(roleService.updateRole(testRoleId, "SUPER_USER")).thenReturn(testRole);

        mockMvc.perform(put("/api/roles/{roleId}", testRoleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated successfully"));

        verify(roleService).updateRole(testRoleId, "SUPER_USER");
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() throws Exception {
        doNothing().when(roleService).deleteRole(testRoleId);

        mockMvc.perform(delete("/api/roles/{roleId}", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));

        verify(roleService).deleteRole(testRoleId);
    }

    @Test
    @DisplayName("Should return 409 when trying to delete role with assigned users")
    void shouldReturn409WhenDeletingRoleWithAssignedUsers() throws Exception {
        doThrow(new IllegalStateException("Cannot delete role USER as it has users assigned"))
                .when(roleService).deleteRole(testRoleId);

        mockMvc.perform(delete("/api/roles/{roleId}", testRoleId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete role USER as it has users assigned"));

        verify(roleService).deleteRole(testRoleId);
    }
}
