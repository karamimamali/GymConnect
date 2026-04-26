package com.gymconnect.facade;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import com.gymconnect.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymFacade gymFacade;

    @Test
    void createTrainee_shouldDelegateToTraineeService() {
        LocalDate dob = LocalDate.of(1995, 6, 15);
        Trainee expected = new Trainee("John", "Smith", true, dob, "123 Main St");
        when(traineeService.createTrainee("John", "Smith", dob, "123 Main St")).thenReturn(expected);

        Trainee result = gymFacade.createTrainee("John", "Smith", dob, "123 Main St");

        assertEquals(expected, result);
        verify(traineeService).createTrainee("John", "Smith", dob, "123 Main St");
    }

    @Test
    void updateTrainee_shouldDelegateToTraineeService() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        when(traineeService.updateTrainee(trainee)).thenReturn(trainee);

        Trainee result = gymFacade.updateTrainee(trainee);

        assertEquals(trainee, result);
        verify(traineeService).updateTrainee(trainee);
    }

    @Test
    void deleteTrainee_shouldDelegateToTraineeService() {
        when(traineeService.deleteTrainee(1L)).thenReturn(true);

        boolean result = gymFacade.deleteTrainee(1L);

        assertTrue(result);
        verify(traineeService).deleteTrainee(1L);
    }

    @Test
    void selectTrainee_shouldDelegateToTraineeService() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        when(traineeService.selectTrainee(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = gymFacade.selectTrainee(1L);

        assertTrue(result.isPresent());
        verify(traineeService).selectTrainee(1L);
    }

    @Test
    void createTrainer_shouldDelegateToTrainerService() {
        Trainer expected = new Trainer("Mike", "Johnson", true, TrainingType.FITNESS);
        when(trainerService.createTrainer("Mike", "Johnson", TrainingType.FITNESS)).thenReturn(expected);

        Trainer result = gymFacade.createTrainer("Mike", "Johnson", TrainingType.FITNESS);

        assertEquals(expected, result);
        verify(trainerService).createTrainer("Mike", "Johnson", TrainingType.FITNESS);
    }

    @Test
    void updateTrainer_shouldDelegateToTrainerService() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerService.updateTrainer(trainer)).thenReturn(trainer);

        Trainer result = gymFacade.updateTrainer(trainer);

        assertEquals(trainer, result);
        verify(trainerService).updateTrainer(trainer);
    }

    @Test
    void selectTrainer_shouldDelegateToTrainerService() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerService.selectTrainer(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = gymFacade.selectTrainer(1L);

        assertTrue(result.isPresent());
        verify(trainerService).selectTrainer(1L);
    }

    @Test
    void createTraining_shouldDelegateToTrainingService() {
        LocalDate date = LocalDate.of(2026, 4, 28);
        Training expected = new Training(1L, 1L, "Morning Fitness", TrainingType.FITNESS, date, 60);
        when(trainingService.createTraining(1L, 1L, "Morning Fitness", TrainingType.FITNESS, date, 60))
                .thenReturn(expected);

        Training result = gymFacade.createTraining(1L, 1L, "Morning Fitness", TrainingType.FITNESS, date, 60);

        assertEquals(expected, result);
        verify(trainingService).createTraining(1L, 1L, "Morning Fitness", TrainingType.FITNESS, date, 60);
    }

    @Test
    void selectTraining_shouldDelegateToTrainingService() {
        Training training = new Training();
        training.setId(1L);
        when(trainingService.selectTraining(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = gymFacade.selectTraining(1L);

        assertTrue(result.isPresent());
        verify(trainingService).selectTraining(1L);
    }
}
