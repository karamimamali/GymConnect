package com.gymconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.dto.ActivateDeactivateRequest;
import com.gymconnect.dto.AddTrainingRequest;
import com.gymconnect.dto.TraineeRegistrationRequest;
import com.gymconnect.dto.UpdateTraineeRequest;
import com.gymconnect.dto.UpdateTraineeTrainersRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TraineeController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private Trainee createTrainee(String firstName, String lastName, String username,
                                   String password) {
        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword(password);
        user.setRawPassword(password);
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Address");
        trainee.setTrainers(List.of());
        return trainee;
    }

    private Trainer createTrainer(String firstName, String lastName, String username) {
        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword("pass");
        TrainingType spec = new TrainingType("FITNESS");
        Trainer trainer = new Trainer(user, spec);
        trainer.setTrainees(List.of());
        return trainer;
    }

    @Test
    void registerReturnsCredentials() throws Exception {
        Trainee trainee = createTrainee("Alice", "Brown", "Alice.Brown", "genPass");
        when(gymFacade.createTrainee("Alice", "Brown", LocalDate.of(2000, 1, 1), "City"))
                .thenReturn(trainee);

        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "Alice", "Brown", LocalDate.of(2000, 1, 1), "City");

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Alice.Brown"))
                .andExpect(jsonPath("$.password").value("genPass"));
    }

    @Test
    void registerWithBlankFirstNameReturns400() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "", "Brown", null, null);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProfileReturnsTraineeData() throws Exception {
        Trainee trainee = createTrainee("Alice", "Brown", "Alice.Brown", "pass");
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis");
        trainee.setTrainers(List.of(trainer));

        when(gymFacade.getTraineeByUsername("Alice.Brown")).thenReturn(trainee);

        mockMvc.perform(get("/api/trainees/Alice.Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers[0].username").value("Bob.Davis"));
    }

    @Test
    void getProfileNotFoundReturns404() throws Exception {
        when(gymFacade.getTraineeByUsername("unknown"))
                .thenThrow(new IllegalArgumentException("Trainee not found: unknown"));

        mockMvc.perform(get("/api/trainees/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfileReturnsUpdatedData() throws Exception {
        Trainee trainee = createTrainee("Alice", "Smith", "Alice.Brown", "pass");
        trainee.setTrainers(List.of());

        when(gymFacade.updateTrainee("Alice.Brown", "Alice", "Smith",
                LocalDate.of(2000, 1, 1), "New Address", true)).thenReturn(trainee);

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                "Alice.Brown", "Alice", "Smith", LocalDate.of(2000, 1, 1),
                "New Address", true);

        mockMvc.perform(put("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Alice.Brown"))
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void deleteProfileReturns200() throws Exception {
        doNothing().when(gymFacade).deleteTraineeByUsername("Alice.Brown");

        mockMvc.perform(delete("/api/trainees/Alice.Brown"))
                .andExpect(status().isOk());

        verify(gymFacade).deleteTraineeByUsername("Alice.Brown");
    }

    @Test
    void getNotAssignedTrainersReturnsList() throws Exception {
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis");

        when(gymFacade.getUnassignedTrainers("Alice.Brown")).thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/trainees/Alice.Brown/not-assigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Bob.Davis"))
                .andExpect(jsonPath("$[0].specialization").value("FITNESS"));
    }

    @Test
    void updateTrainersReturnsList() throws Exception {
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis");
        Trainee trainee = createTrainee("Alice", "Brown", "Alice.Brown", "pass");
        trainee.setTrainers(List.of(trainer));

        doNothing().when(gymFacade).updateTraineeTrainers("Alice.Brown", List.of("Bob.Davis"));
        when(gymFacade.getTraineeByUsername("Alice.Brown")).thenReturn(trainee);

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                List.of("Bob.Davis"));

        mockMvc.perform(put("/api/trainees/Alice.Brown/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Bob.Davis"));
    }

    @Test
    void getTrainingsReturnsList() throws Exception {
        TrainingType type = new TrainingType("FITNESS");
        Trainee trainee = createTrainee("Alice", "Brown", "Alice.Brown", "pass");
        Trainer trainer = createTrainer("Bob", "Davis", "Bob.Davis");
        Training training = new Training(trainee, trainer, "Morning Session",
                type, LocalDate.of(2026, 6, 1), 60);

        when(gymFacade.getTraineeTrainings("Alice.Brown", null, null, null, null))
                .thenReturn(List.of(training));

        mockMvc.perform(get("/api/trainees/Alice.Brown/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Session"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Bob Davis"));
    }

    @Test
    void addTrainingReturns200() throws Exception {
        Training training = new Training();
        when(gymFacade.addTraining(eq("Alice.Brown"), eq("Bob.Davis"),
                eq("Morning"), any(LocalDate.class), eq(60))).thenReturn(training);

        AddTrainingRequest request = new AddTrainingRequest(
                "Alice.Brown", "Bob.Davis", "Morning", LocalDate.of(2026, 6, 1), 60);

        mockMvc.perform(post("/api/trainees/Alice.Brown/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void activateTraineeReturns200() throws Exception {
        doNothing().when(gymFacade).activateTrainee("Alice.Brown");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);

        mockMvc.perform(patch("/api/trainees/Alice.Brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).activateTrainee("Alice.Brown");
    }

    @Test
    void deactivateTraineeReturns200() throws Exception {
        doNothing().when(gymFacade).deactivateTrainee("Alice.Brown");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(false);

        mockMvc.perform(patch("/api/trainees/Alice.Brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).deactivateTrainee("Alice.Brown");
    }

    @Test
    void activateAlreadyActiveReturns400() throws Exception {
        doThrow(new IllegalStateException("Trainee is already active: Alice.Brown"))
                .when(gymFacade).activateTrainee("Alice.Brown");

        ActivateDeactivateRequest request = new ActivateDeactivateRequest(true);

        mockMvc.perform(patch("/api/trainees/Alice.Brown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
