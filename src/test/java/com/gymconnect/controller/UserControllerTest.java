package com.gymconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.dto.ChangePasswordRequest;
import com.gymconnect.dto.LoginRequest;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.security.JwtTokenProvider;
import com.gymconnect.security.LoginAttemptService;
import com.gymconnect.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private GymFacade gymFacade;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserController(gymFacade, authenticationManager, jwtTokenProvider,
                        loginAttemptService, tokenBlacklistService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginSuccessReturns200WithToken() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        when(loginAttemptService.isBlocked("user1")).thenReturn(false);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateToken("user1")).thenReturn("jwt-token-123");

        LoginRequest request = new LoginRequest("user1", "pass1");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(loginAttemptService).loginSucceeded("user1");
    }

    @Test
    void loginFailedReturns401() throws Exception {
        when(loginAttemptService.isBlocked("user1")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        LoginRequest request = new LoginRequest("user1", "wrong");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(loginAttemptService).loginFailed("user1");
    }

    @Test
    void loginBlockedReturns423() throws Exception {
        when(loginAttemptService.isBlocked("user1")).thenReturn(true);

        LoginRequest request = new LoginRequest("user1", "pass1");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked());
    }

    @Test
    void loginMissingFieldReturns400() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutReturns200() throws Exception {
        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer some-token"))
                .andExpect(status().isOk());

        verify(tokenBlacklistService).blacklist("some-token");
    }

    @Test
    void changePasswordReturns200() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isOk());

        verify(gymFacade).changePassword("user1", "oldPass", "newPass");
    }

    @Test
    void changePasswordInvalidOldReturns401() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        doThrow(new SecurityException("Invalid old password"))
                .when(gymFacade).changePassword("user1", "wrong", "newPass");

        ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newPass");

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordBlankFieldsReturns400() throws Exception {
        Authentication auth = mock(Authentication.class);

        ChangePasswordRequest request = new ChangePasswordRequest("", "newPass");

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(auth))
                .andExpect(status().isBadRequest());
    }
}
