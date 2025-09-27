package com.user.service.users_service.service;

import com.user.service.users_service.model.Role;
import com.user.service.users_service.repository.RoleRepository;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public Role createRole(String name) {
        if (roleRepository.existsByName(name.toUpperCase())) {
            throw new AlreadyExistsException("Role " + name + " already exists");
        }

        Role role = new Role(name);
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully: {}", savedRole.getName());
        return savedRole;
    }

    @Transactional(readOnly = true)
    public Role getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name.toUpperCase())
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role updateRole(UUID roleId, String name) {
        Role role = getRoleById(roleId);
        if (name != null && !name.equals(role.getName())) {
            if (roleRepository.existsByName(name.toUpperCase())) {
                throw new AlreadyExistsException("Role " + name + " already exists");
            }
            role.setName(name.toUpperCase());
        }
        Role updatedRole = roleRepository.save(role);
        log.info("Role updated successfully: {}", updatedRole.getName());
        return updatedRole;
    }

    public void deleteRole(UUID roleId) {
        Role role = getRoleById(roleId);
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role " + role.getName() + " because it has users assigned");
        }
        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", role.getName());
    }
}
