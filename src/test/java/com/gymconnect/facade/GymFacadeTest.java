package com.gymconnect.facade;

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
import static org.mockito.ArgumentMatchers.any;
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

    private GymFacade gymFacade;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType fitnessType;
    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Mike.Johnson";
    private static final String PASSWORD = "testPass123";

    @BeforeEach
    void setUp() {
        gymFacade = new GymFacade(traineeService, trainerService, trainingService);

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
    void getTraineeByUsername_shouldReturnTrainee_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getTraineeByUsername(TRAINEE_USERNAME))
                .thenReturn(Optional.of(trainee));

        Trainee result = gymFacade.getTraineeByUsername(TRAINEE_USERNAME, PASSWORD);

        assertNotNull(result);
        assertEquals(TRAINEE_USERNAME, result.getUser().getUsername());
    }

    @Test
    void getTraineeByUsername_shouldThrow_whenNotAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.getTraineeByUsername(TRAINEE_USERNAME, "wrong"));
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainer_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);
        when(trainerService.getTrainerByUsername(TRAINER_USERNAME))
                .thenReturn(Optional.of(trainer));

        Trainer result = gymFacade.getTrainerByUsername(TRAINER_USERNAME, PASSWORD);

        assertNotNull(result);
    }

    @Test
    void getTrainerByUsername_shouldThrow_whenNotAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.getTrainerByUsername(TRAINER_USERNAME, "wrong"));
    }

    @Test
    void changeTraineePassword_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.changeTraineePassword(TRAINEE_USERNAME, PASSWORD, "newPass1234");

        verify(traineeService).changePassword(TRAINEE_USERNAME, "newPass1234");
    }

    @Test
    void changeTraineePassword_shouldThrow_whenNotAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.changeTraineePassword(TRAINEE_USERNAME, "wrong", "newPass"));
    }

    @Test
    void changeTrainerPassword_shouldSucceed_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.changeTrainerPassword(TRAINER_USERNAME, PASSWORD, "newPass1234");

        verify(trainerService).changePassword(TRAINER_USERNAME, "newPass1234");
    }

    @Test
    void updateTrainee_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.updateTrainee(TRAINEE_USERNAME, "Johnny", "Doe",
                LocalDate.of(1995, 6, 15), "New Address", true)).thenReturn(trainee);

        Trainee result = gymFacade.updateTrainee(TRAINEE_USERNAME, PASSWORD,
                "Johnny", "Doe", LocalDate.of(1995, 6, 15), "New Address", true);

        assertNotNull(result);
    }

    @Test
    void updateTrainer_shouldSucceed_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);
        when(trainerService.updateTrainer(TRAINER_USERNAME, "Michael", "Johnson",
                "FITNESS", true)).thenReturn(trainer);

        Trainer result = gymFacade.updateTrainer(TRAINER_USERNAME, PASSWORD,
                "Michael", "Johnson", "FITNESS", true);

        assertNotNull(result);
    }

    @Test
    void activateTrainee_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.activateTrainee(TRAINEE_USERNAME, PASSWORD);

        verify(traineeService).activateTrainee(TRAINEE_USERNAME);
    }

    @Test
    void deactivateTrainee_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.deactivateTrainee(TRAINEE_USERNAME, PASSWORD);

        verify(traineeService).deactivateTrainee(TRAINEE_USERNAME);
    }

    @Test
    void activateTrainer_shouldSucceed_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.activateTrainer(TRAINER_USERNAME, PASSWORD);

        verify(trainerService).activateTrainer(TRAINER_USERNAME);
    }

    @Test
    void deactivateTrainer_shouldSucceed_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.deactivateTrainer(TRAINER_USERNAME, PASSWORD);

        verify(trainerService).deactivateTrainer(TRAINER_USERNAME);
    }

    @Test
    void deleteTraineeByUsername_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.deleteTraineeByUsername(TRAINEE_USERNAME, PASSWORD);

        verify(traineeService).deleteTraineeByUsername(TRAINEE_USERNAME);
    }

    @Test
    void deleteTraineeByUsername_shouldThrow_whenNotAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.deleteTraineeByUsername(TRAINEE_USERNAME, "wrong"));
    }

    @Test
    void getTraineeTrainings_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getTraineeTrainings(TRAINEE_USERNAME, null, null, null, null))
                .thenReturn(List.of(new Training()));

        List<Training> result = gymFacade.getTraineeTrainings(
                TRAINEE_USERNAME, PASSWORD, null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTrainerTrainings_shouldSucceed_whenAuthenticated() {
        when(trainerService.authenticate(TRAINER_USERNAME, PASSWORD)).thenReturn(true);
        when(trainerService.getTrainerTrainings(TRAINER_USERNAME, null, null, null))
                .thenReturn(List.of(new Training()));

        List<Training> result = gymFacade.getTrainerTrainings(
                TRAINER_USERNAME, PASSWORD, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void addTraining_shouldSucceed_whenAuthenticated() {
        Training training = new Training(trainee, trainer, "Session",
                fitnessType, LocalDate.of(2026, 5, 1), 60);
        training.setId(1L);

        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);
        when(trainingService.addTraining(TRAINEE_USERNAME, TRAINER_USERNAME,
                "Session", "FITNESS", LocalDate.of(2026, 5, 1), 60))
                .thenReturn(training);

        Training result = gymFacade.addTraining(TRAINEE_USERNAME, PASSWORD,
                TRAINER_USERNAME, "Session", "FITNESS", LocalDate.of(2026, 5, 1), 60);

        assertNotNull(result);
    }

    @Test
    void addTraining_shouldThrow_whenNotAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.addTraining(TRAINEE_USERNAME, "wrong", TRAINER_USERNAME,
                        "Session", "FITNESS", LocalDate.of(2026, 5, 1), 60));
    }

    @Test
    void getUnassignedTrainers_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of(trainer));

        List<Trainer> result = gymFacade.getUnassignedTrainers(TRAINEE_USERNAME, PASSWORD);

        assertEquals(1, result.size());
    }

    @Test
    void updateTraineeTrainers_shouldSucceed_whenAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, PASSWORD)).thenReturn(true);

        gymFacade.updateTraineeTrainers(TRAINEE_USERNAME, PASSWORD,
                List.of(TRAINER_USERNAME));

        verify(traineeService).updateTraineeTrainers(TRAINEE_USERNAME,
                List.of(TRAINER_USERNAME));
    }

    @Test
    void updateTraineeTrainers_shouldThrow_whenNotAuthenticated() {
        when(traineeService.authenticate(TRAINEE_USERNAME, "wrong")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> gymFacade.updateTraineeTrainers(TRAINEE_USERNAME, "wrong",
                        List.of(TRAINER_USERNAME)));
    }
}
