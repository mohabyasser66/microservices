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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;
    private UserDto testUserDto;
    private UUID testUserId;
    private UUID testRoleId;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Set up MockMvc with standalone setup
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        testUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();

        // Create test role
        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("USER");

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
        // Initialize roles collection
        Collection<Role> roles = new HashSet<>();
        roles.add(testRole);
        testUser.setRoles(roles);

        testUserDto = new UserDto();
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setEmail("john.doe@example.com");
    }

    // ============ BASIC CRUD TESTS ============

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() throws Exception {
        when(userService.getUserById(testUserId)).thenReturn(testUser);
        when(userService.convertUserToDto(testUser)).thenReturn(testUserDto);

        mockMvc.perform(get("/api/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(userService).getUserById(testUserId);
        verify(userService).convertUserToDto(testUser);
    }

    @Test
    @DisplayName("Should get all users successfully - working solution")
    void shouldGetAllUsersSuccessfully() throws Exception {
        // Create proper empty page that can be serialized
        List<UserDto> emptyList = new ArrayList<>();
        Page<UserDto> workingPage = new PageImpl<>(emptyList);
        when(userService.getAllUsers(0, 10, "createdAt", "desc", null)).thenReturn(workingPage);

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("sortDir", "desc"))
                .andDo(result -> {
                    // Add debugging output
                    System.out.println("✅ Test Debug - Response Status: " + result.getResponse().getStatus());
                    System.out.println("✅ Test Debug - Response Content: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));

        verify(userService).getAllUsers(0, 10, "createdAt", "desc", null);
    }

    @Test
    @DisplayName("Should search users successfully - working solution")
    void shouldSearchUsersSuccessfully() throws Exception {
        // Create proper empty page that can be serialized
        List<UserDto> emptyList = new ArrayList<>();
        Page<UserDto> workingPage = new PageImpl<>(emptyList);
        when(userService.searchUsers("john", null, null, null, 0, 10)).thenReturn(workingPage);

        mockMvc.perform(get("/api/users/search")
                .param("email", "john")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Search completed successfully"));

        verify(userService).searchUsers("john", null, null, null, 0, 10);
    }

    @Test
    @DisplayName("Should get users by role successfully - working solution")
    void shouldGetUsersByRoleSuccessfully() throws Exception {
        // Create proper empty page that can be serialized
        List<UserDto> emptyList = new ArrayList<>();
        Page<UserDto> workingPage = new PageImpl<>(emptyList);
        when(userService.getUsersByRole("USER", 0, 10)).thenReturn(workingPage);

        mockMvc.perform(get("/api/users/by-role/{roleName}", "USER")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"));

        verify(userService).getUsersByRole("USER", 0, 10);
    }

    @Test
    @DisplayName("Should get user login history successfully - working solution")
    void shouldGetUserLoginHistorySuccessfully() throws Exception {
        // Create proper empty page that can be serialized
        List<LoginHistoryDto> emptyList = new ArrayList<>();
        Page<LoginHistoryDto> workingPage = new PageImpl<>(emptyList);
        when(userService.getUserLoginHistory(testUserId, 0, 10)).thenReturn(workingPage);

        mockMvc.perform(get("/api/users/{userId}/login-history", testUserId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login history retrieved successfully"));

        verify(userService).getUserLoginHistory(testUserId, 0, 10);
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
                .andExpect(jsonPath("$.message").value("User created successfully"));

        verify(userService).createUser(any(CreateUserRequest.class));
        verify(userService).convertUserToDto(testUser);
    }

    @Test
    @DisplayName("Should handle user not found exception")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(testUserId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{userId}", testUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUserById(testUserId);
    }

    @Test
    @DisplayName("Should handle user already exists exception")
    void shouldHandleUserAlreadyExistsException() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("password123");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new AlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));

        verify(userService).createUser(any(CreateUserRequest.class));
    }

    // Add more simplified tests as needed...
}
