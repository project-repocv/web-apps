package com.bank.authservice.service;

import com.bank.authservice.dto.RegisterRequest;
import com.bank.authservice.entity.User;
import com.bank.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        String username = "testuser";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .customerId("customer123")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.ACTIVE)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> 
            userService.loadUserByUsername(username));
        verify(userRepository).findByUsername(username);
    }

    @Test
    void createUser_ValidRequest_CreatesUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .customerId("customer456")
                .email("test@example.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("newuser")
                .password("encodedPassword")
                .customerId("customer456")
                .roles(Set.of("ROLE_CUSTOMER"))
                .status(User.UserStatus.PENDING_ACTIVATION)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByCustomerId("customer456")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("customer456", result.getCustomerId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .customerId("customer456")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_CustomerIdAlreadyExists_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .customerId("existingcustomer")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByCustomerId("existingcustomer")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> 
            userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        String username = "testuser";
        User expectedUser = User.builder().id(1L).username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(expectedUser));

        User result = userService.findByUsername(username);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByUsername_UserNotExists_ReturnsNull() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        User result = userService.findByUsername(username);

        assertNull(result);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void activateUser_UserExists_ActivatesUser() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .status(User.UserStatus.PENDING_ACTIVATION)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.activateUser(userId);

        assertNotNull(result);
        assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void suspendUser_UserExists_SuspendsUser() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .status(User.UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.suspendUser(userId);

        assertNotNull(result);
        assertEquals(User.UserStatus.SUSPENDED, result.getStatus());
        verify(userRepository).save(user);
    }
}
