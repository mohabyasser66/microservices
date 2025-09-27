package com.user.service.users_service.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.service.users_service.dto.*;
import com.user.service.users_service.exceptions.AlreadyExistsException;
import com.user.service.users_service.exceptions.ResourceNotFoundException;
import com.user.service.users_service.model.Role;
import com.user.service.users_service.model.User;
import com.user.service.users_service.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Pure Unit Tests")
class UserControllerPureUnitTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private UserDto testUserDto;
    private UUID testUserId;
    private UUID testRoleId;
    private Role testRole;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        testUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
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

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(testUser);
        when(userService.convertUserToDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(get("/api/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        verify(userService).getUserById(testUserId);
        verify(userService).convertUserToDto(testUser);
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);
        when(userService.convertUserToDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(post("/api/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(userService).createUser(any(CreateUserRequest.class));
        verify(userService).convertUserToDto(testUser);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(testUserId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{userId}", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUserById(testUserId);
    }

    @Test
    @DisplayName("Should return 409 when user already exists")
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("john.doe@example.com");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new AlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        doNothing().when(userService).deleteUser(testUserId);

        mockMvc.perform(delete("/api/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService).deleteUser(testUserId);
    }
}
