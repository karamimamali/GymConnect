package com.gymconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.dto.ChangePasswordRequest;
import com.gymconnect.facade.GymFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginAsTraineeReturns200() throws Exception {
        when(gymFacade.authenticateTrainee("user1", "pass1")).thenReturn(true);

        mockMvc.perform(get("/api/users/login")
                        .param("username", "user1")
                        .param("password", "pass1"))
                .andExpect(status().isOk());
    }

    @Test
    void loginAsTrainerReturns200() throws Exception {
        when(gymFacade.authenticateTrainee("trainer1", "pass1")).thenReturn(false);
        when(gymFacade.authenticateTrainer("trainer1", "pass1")).thenReturn(true);

        mockMvc.perform(get("/api/users/login")
                        .param("username", "trainer1")
                        .param("password", "pass1"))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        when(gymFacade.authenticateTrainee("user1", "wrong")).thenReturn(false);
        when(gymFacade.authenticateTrainer("user1", "wrong")).thenReturn(false);

        mockMvc.perform(get("/api/users/login")
                        .param("username", "user1")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginMissingParamReturns400() throws Exception {
        mockMvc.perform(get("/api/users/login")
                        .param("username", "user1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeTraineePasswordReturns200() throws Exception {
        when(gymFacade.authenticateTrainee("user1", "oldPass")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("user1", "oldPass", "newPass");

        mockMvc.perform(put("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changeTraineePassword("user1", "oldPass", "newPass");
    }

    @Test
    void changeTrainerPasswordReturns200() throws Exception {
        when(gymFacade.authenticateTrainee("trainer1", "oldPass")).thenReturn(false);
        when(gymFacade.authenticateTrainer("trainer1", "oldPass")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("trainer1", "oldPass", "newPass");

        mockMvc.perform(put("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changeTrainerPassword("trainer1", "oldPass", "newPass");
    }

    @Test
    void changePasswordWithInvalidCredentialsReturns401() throws Exception {
        when(gymFacade.authenticateTrainee("user1", "wrong")).thenReturn(false);
        when(gymFacade.authenticateTrainer("user1", "wrong")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("user1", "wrong", "newPass");

        mockMvc.perform(put("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithBlankFieldsReturns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("", "oldPass", "newPass");

        mockMvc.perform(put("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
