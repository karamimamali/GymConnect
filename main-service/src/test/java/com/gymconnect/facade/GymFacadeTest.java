package com.gymconnect.facade;

import com.gymconnect.client.WorkloadGateway;
import com.gymconnect.dto.WorkloadActionType;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import com.gymconnect.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private WorkloadGateway workloadGateway;

    private GymFacade gymFacade;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType fitnessType;
    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Mike.Johnson";
    private static final String PASSWORD = "testPass123";

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(traineeService, trainerService, trainingService, workloadGateway);

        fitnessType = new TrainingType("FITNESS");
        fitnessType.setId(1L);

        User traineeUser = new User("John", "Doe", true);
        traineeUser.setId(1L);
        traineeUser.setUsername(TRAINEE_USERNAME);
        traineeUser.setPassword(PASSWORD);
        trainee = new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St");
        trainee.setId(1L);

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setId(2L);
        trainerUser.setUsername(TRAINER_USERNAME);
        trainerUser.setPassword(PASSWORD);
        trainer = new Trainer(trainerUser, fitnessType);
        trainer.setId(1L);
    }

    @Test
    void createTrainee_shouldDelegateToService() {
        when(traineeService.createTrainee("John", "Doe", LocalDate.of(1995, 6, 15), "123 Main St"))
                .thenReturn(trainee);

        Trainee result = gymFacade.createTrainee("John", "Doe",
                LocalDate.of(1995, 6, 15), "123 Main St");

        assertNotNull(result);
        assertEquals(TRAINEE_USERNAME, result.getUser().getUsername());
    }

    @Test
    void createTrainer_shouldDelegateToService() {
        when(trainerService.createTrainer("Mike", "Johnson", "FITNESS"))
                .thenReturn(trainer);

        Trainer result = gymFacade.createTrainer("Mike", "Johnson", "FITNESS");

        assertNotNull(result);
        assertEquals(TRAINER_USERNAME, result.getUser().getUsername());
    }

    @Test
    void authenticateTrainee_shouldReturnTrue_whenValid() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        assertTrue(gymFacade.authenticateTrainee(TRAINEE_USERNAME, PASSWORD));
    }

    @Test
    void authenticateTrainee_shouldReturnFalse_whenInvalid() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertFalse(gymFacade.authenticateTrainee(TRAINEE_USERNAME, "wrong"));
    }

    @Test
    void authenticateTrainer_shouldReturnTrue_whenValid() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);

        assertTrue(gymFacade.authenticateTrainer(TRAINER_USERNAME, PASSWORD));
    }

    @Test
    void getTraineeByUsername_shouldReturnTrainee() {
        when(traineeService.getTraineeByUsername(TRAINEE_USERNAME))
                .thenReturn(Optional.of(trainee));

        Trainee result = gymFacade.getTraineeByUsername(TRAINEE_USERNAME);

        assertNotNull(result);
        assertEquals(TRAINEE_USERNAME, result.getUser().getUsername());
    }

    @Test
    void getTraineeByUsername_shouldThrow_whenNotFound() {
        when(traineeService.getTraineeByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> gymFacade.getTraineeByUsername("unknown"));
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainer() {
        when(trainerService.getTrainerByUsername(TRAINER_USERNAME))
                .thenReturn(Optional.of(trainer));

        Trainer result = gymFacade.getTrainerByUsername(TRAINER_USERNAME);

        assertNotNull(result);
    }

    @Test
    void changePassword_shouldSucceed_whenTraineeOldPasswordValid() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.changePassword(TRAINEE_USERNAME, PASSWORD, "newPass1234");

        verify(traineeService).changePassword(TRAINEE_USERNAME, "newPass1234");
    }

    @Test
    void changePassword_shouldSucceed_whenTrainerOldPasswordValid() {
        when(traineeService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(false);
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.changePassword(TRAINER_USERNAME, PASSWORD, "newPass1234");

        verify(trainerService).changePassword(TRAINER_USERNAME, "newPass1234");
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordInvalid() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);
        when(trainerService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.changePassword(TRAINEE_USERNAME, "wrong", "newPass"));
    }

    @Test
    void updateTrainee_shouldDelegateToService() {
        when(traineeService.updateTrainee(TRAINEE_USERNAME, "Johnny", "Doe",
                LocalDate.of(1995, 6, 15), "New Address", true)).thenReturn(trainee);

        Trainee result = gymFacade.updateTrainee(TRAINEE_USERNAME,
                "Johnny", "Doe", LocalDate.of(1995, 6, 15), "New Address", true);

        assertNotNull(result);
    }

    @Test
    void updateTrainer_shouldDelegateToService() {
        when(trainerService.updateTrainer(TRAINER_USERNAME, "Michael", "Johnson",
                "FITNESS", true)).thenReturn(trainer);

        Trainer result = gymFacade.updateTrainer(TRAINER_USERNAME,
                "Michael", "Johnson", "FITNESS", true);

        assertNotNull(result);
    }

    @Test
    void activateTrainee_shouldDelegateToService() {
        gymFacade.activateTrainee(TRAINEE_USERNAME);

        verify(traineeService).activateTrainee(TRAINEE_USERNAME);
    }

    @Test
    void deactivateTrainee_shouldDelegateToService() {
        gymFacade.deactivateTrainee(TRAINEE_USERNAME);

        verify(traineeService).deactivateTrainee(TRAINEE_USERNAME);
    }

    @Test
    void activateTrainer_shouldDelegateToService() {
        gymFacade.activateTrainer(TRAINER_USERNAME);

        verify(trainerService).activateTrainer(TRAINER_USERNAME);
    }

    @Test
    void deactivateTrainer_shouldDelegateToService() {
        gymFacade.deactivateTrainer(TRAINER_USERNAME);

        verify(trainerService).deactivateTrainer(TRAINER_USERNAME);
    }

    @Test
    void deleteTraineeByUsername_shouldDeleteAndReverseEachTrainingWorkload() {
        Training training = new Training(trainee, trainer, "Session",
                fitnessType, LocalDate.of(2026, 5, 1), 60);
        when(traineeService.getTraineeTrainings(TRAINEE_USERNAME, null, null, null, null))
                .thenReturn(List.of(training));

        gymFacade.deleteTraineeByUsername(TRAINEE_USERNAME);

        verify(traineeService).deleteTraineeByUsername(TRAINEE_USERNAME);
        verify(workloadGateway).notify(WorkloadActionType.DELETE, training);
    }

    @Test
    void getTraineeTrainings_shouldDelegateToService() {
        when(traineeService.getTraineeTrainings(TRAINEE_USERNAME, null, null, null, null))
                .thenReturn(List.of(new Training()));

        List<Training> result = gymFacade.getTraineeTrainings(
                TRAINEE_USERNAME, null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldDelegateToService() {
        when(trainerService.getTrainerTrainings(TRAINER_USERNAME, null, null, null))
                .thenReturn(List.of(new Training()));

        List<Training> result = gymFacade.getTrainerTrainings(
                TRAINER_USERNAME, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void addTraining_shouldDelegateToService() {
        Training training = new Training(trainee, trainer, "Session",
                fitnessType, LocalDate.of(2026, 5, 1), 60);

        when(trainerService.getTrainerByUsername(TRAINER_USERNAME))
                .thenReturn(Optional.of(trainer));
        when(trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                "Session", "FITNESS", LocalDate.of(2026, 5, 1), 60))
                .thenReturn(training);

        Training result = gymFacade.addTraining(TRAINEE_USERNAME,
                TRAINER_USERNAME, "Session", LocalDate.of(2026, 5, 1), 60);

        assertNotNull(result);
        verify(workloadGateway).notify(WorkloadActionType.ADD, training);
    }

    @Test
    void addTraining_shouldThrow_whenTrainerNotFound() {
        when(trainerService.getTrainerByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> gymFacade.addTraining(TRAINEE_USERNAME, "unknown",
                        "Session", LocalDate.of(2026, 5, 1), 60));
    }

    @Test
    void getUnassignedTrainers_shouldDelegateToService() {
        when(traineeService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of(trainer));

        List<Trainer> result = gymFacade.getUnassignedTrainers(TRAINEE_USERNAME);

        assertEquals(1, result.size());
    }

    @Test
    void updateTraineeTrainers_shouldDelegateToService() {
        gymFacade.updateTraineeTrainers(TRAINEE_USERNAME, List.of(TRAINER_USERNAME));

        verify(traineeService).updateTraineeTrainers(TRAINEE_USERNAME,
                List.of(TRAINER_USERNAME));
    }
}
