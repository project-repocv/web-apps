package com.bank.authservice.controller;

import com.bank.authservice.dto.LoginRequest;
import com.bank.authservice.dto.JwtResponse;
import com.bank.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureWebMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void authenticateUser_ValidCredentials_ReturnsToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken("mocked-access-token")
                .refreshToken("mocked-refresh-token")
                .customerId("customer123")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .roles(java.util.List.of("ROLE_CUSTOMER"))
                .build();

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "testuser",
                        "password": "password123"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("mocked-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mocked-refresh-token"))
                .andExpect(jsonPath("$.customerId").value("customer123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void authenticateUser_MissingCredentials_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "",
                        "password": ""
                    }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_ValidRequest_ReturnsToken() throws Exception {
        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken("mocked-access-token")
                .refreshToken("mocked-refresh-token")
                .customerId("customer456")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .roles(java.util.List.of("ROLE_CUSTOMER"))
                .build();

        when(authService.register(any())).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "newuser",
                        "password": "password123",
                        "customerId": "customer456",
                        "email": "test@example.com"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked-access-token"))
                .andExpect(jsonPath("$.customerId").value("customer456"));
    }

    @Test
    void refreshToken_ValidRequest_ReturnsNewToken() throws Exception {
        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .customerId("customer123")
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .build();

        when(authService.refreshToken(any())).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "refreshToken": "valid-refresh-token"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }
}
