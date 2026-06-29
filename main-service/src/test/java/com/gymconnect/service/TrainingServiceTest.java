package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.dao.TrainingDao;
import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType fitnessType;

    @BeforeEach
    void setUp() {
        fitnessType = new TrainingType("FITNESS");
        fitnessType.setId(1L);

        User traineeUser = new User("John", "Doe", true);
        traineeUser.setId(1L);
        traineeUser.setUsername("John.Doe");
        traineeUser.setPassword("pass");
        trainee = new Trainee(traineeUser, LocalDate.of(1995, 6, 15), "123 Main St");
        trainee.setId(1L);

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setId(2L);
        trainerUser.setUsername("Mike.Johnson");
        trainerUser.setPassword("pass");
        trainer = new Trainer(trainerUser, fitnessType);
        trainer.setId(1L);
    }

    @Test
    void addTraining_shouldCreateSuccessfully() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("FITNESS")).thenReturn(Optional.of(fitnessType));
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> {
            Training t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.addTraining("John.Doe", "Mike.Johnson",
                "Morning Fitness", "FITNESS", LocalDate.of(2026, 5, 1), 60);

        assertNotNull(result);
        assertEquals("Morning Fitness", result.getTrainingName());
        assertEquals(60, result.getTrainingDuration());
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void addTraining_shouldThrow_whenTraineeUsernameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(null, "Mike.Johnson",
                        "Session", "FITNESS", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenTrainerUsernameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "",
                        "Session", "FITNESS", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenTrainingNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "Mike.Johnson",
                        null, "FITNESS", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenTrainingTypeNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "Mike.Johnson",
                        "Session", null, LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenDateNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "Mike.Johnson",
                        "Session", "FITNESS", null, 60));
    }

    @Test
    void addTraining_shouldThrow_whenDurationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "Mike.Johnson",
                        "Session", "FITNESS", LocalDate.now(), null));
    }

    @Test
    void addTraining_shouldThrow_whenTraineeNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("unknown", "Mike.Johnson",
                        "Session", "FITNESS", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenTrainerNotFound() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "unknown",
                        "Session", "FITNESS", LocalDate.now(), 60));
    }

    @Test
    void addTraining_shouldThrow_whenTrainingTypeNotFound() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("John.Doe", "Mike.Johnson",
                        "Session", "UNKNOWN", LocalDate.now(), 60));
    }
}
