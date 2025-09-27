package com.user.service.users_service.Controller;

import com.user.service.users_service.dto.ApiResponse;
import com.user.service.users_service.dto.CreateRoleRequest;
import com.user.service.users_service.dto.UpdateRoleRequest;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.ok(new ApiResponse("Roles retrieved successfully", roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Error retrieving roles", null));
        }
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse> getRoleById(@PathVariable UUID roleId) {
        try {
            Role role = roleService.getRoleById(roleId);
            return ResponseEntity.ok(new ApiResponse("Role retrieved successfully", role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/by-name/{roleName}")
    public ResponseEntity<ApiResponse> getRoleByName(@PathVariable String roleName) {
        try {
            Role role = roleService.getRoleByName(roleName);
            return ResponseEntity.ok(new ApiResponse("Role retrieved successfully", role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        try {
            Role role = roleService.createRole(request.getName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse("Role created successfully", role));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            Role role = roleService.updateRole(roleId, request.getName());
            return ResponseEntity.ok(new ApiResponse("Role updated successfully", role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable UUID roleId) {
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.ok(new ApiResponse("Role deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
