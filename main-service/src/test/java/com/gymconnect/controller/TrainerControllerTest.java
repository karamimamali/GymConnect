package com.gymconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.dto.ActivateDeactivateRequest;
import com.gymconnect.dto.TrainerRegistrationRequest;
import com.gymconnect.dto.UpdateTrainerRequest;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainerController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Trainer createTrainer(String firstName, String lastName, String username,
                                   String password) {
        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword(password);
        user.setRawPassword(password);
        TrainingType spec = new TrainingType("FITNESS");
        Trainer trainer = new Trainer(user, spec);
        trainer.setTrainees(List.of());
        return trainer;
    }

    @Test
    void registerReturnsCredentials() throws Exception {
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis", "genPass");
        when(gymFacade.createTrainer("Bob", "Davis", "FITNESS")).thenReturn(trainer);

        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "Bob", "Davis", "FITNESS");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Bob.Davis"))
                .andExpect(jsonPath("$.password").value("genPass"));
    }

    @Test
    void registerWithBlankNameReturns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                "", "Davis", "FITNESS");

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfileReturnsTrainerData() throws Exception {
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis", "pass");
        when(gymFacade.getTrainerByUsername("Bob.Davis")).thenReturn(trainer);

        mockMvc.perform(get("/api/trainers/Bob.Davis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Davis"))
                .andExpect(jsonPath("$.specialization").value("FITNESS"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getProfileNotFoundReturns404() throws Exception {
        when(gymFacade.getTrainerByUsername("unknown"))
                .thenThrow(new IllegalArgumentException("Trainer not found: unknown"));

        mockMvc.perform(get("/api/trainers/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfileReturnsUpdatedData() throws Exception {
        Trainer trainer = createTrainer("Bob", "Smith", "Bob.Davis", "pass");
        when(gymFacade.getTrainerByUsername("Bob.Davis")).thenReturn(trainer);
        when(gymFacade.updateTrainer("Bob.Davis", "Bob", "Smith", "FITNESS", true))
                .thenReturn(trainer);

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                "Bob.Davis", "Bob", "Smith", true);

        mockMvc.perform(put("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Bob.Davis"))
                .andExpect(jsonPath("$.firstName").value("Bob"));
    }

    @Test
    void getTrainingsReturnsList() throws Exception {
        TrainingType type = new TrainingType("FITNESS");
        User traineeUser = new User("Alice", "Brown", true);
        traineeUser.setUsername("Alice.Brown");
        Trainee trainee = new Trainee(traineeUser, LocalDate.of(2000, 1, 1), "Addr");
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis", "pass");
        Training training = new Training(trainee, trainer, "Morning",
                type, LocalDate.of(2026, 6, 1), 60);

        when(gymFacade.getTrainerTrainings("Bob.Davis", null, null, null))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainers/Bob.Davis/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning"))
                .andExpect(jsonPath("$[0].traineeName").value("Alice Brown"));
    }

    @Test
    void activateTrainerReturns200() throws Exception {
        doNothing().when(gymFacade).activateTrainer("Bob.Davis");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);

        mockMvc.perform(patch("/api/trainers/Bob.Davis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).activateTrainer("Bob.Davis");
    }

    @Test
    void deactivateTrainerReturns200() throws Exception {
        doNothing().when(gymFacade).deactivateTrainer("Bob.Davis");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        mockMvc.perform(patch("/api/trainers/Bob.Davis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).deactivateTrainer("Bob.Davis");
    }

    @Test
    void activateAlreadyActiveReturns400() throws Exception {
        doThrow(new IllegalStateException("Trainer is already active: Bob.Davis"))
                .when(gymFacade).activateTrainer("Bob.Davis");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);

        mockMvc.perform(patch("/api/trainers/Bob.Davis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
