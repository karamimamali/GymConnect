package com.gymconnect.service;

import com.gymconnect.dao.TrainingDao;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDao trainingDao;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createTraining_shouldSaveAndReturnTraining() {
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Training result = trainingService.createTraining(1L, 1L, "Morning Fitness",
                TrainingType.FITNESS, LocalDate.of(2026, 4, 28), 60);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Morning Fitness", result.getTrainingName());
        assertEquals(TrainingType.FITNESS, result.getTrainingType());
        assertEquals(1L, result.getTraineeId());
        assertEquals(1L, result.getTrainerId());
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void createTraining_shouldSetAllFieldsCorrectly() {
        LocalDate date = LocalDate.of(2026, 5, 1);
        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        Training result = trainingService.createTraining(2L, 3L, "Evening Yoga",
                TrainingType.YOGA, date, 45);

        assertEquals(2L, result.getTraineeId());
        assertEquals(3L, result.getTrainerId());
        assertEquals("Evening Yoga", result.getTrainingName());
        assertEquals(TrainingType.YOGA, result.getTrainingType());
        assertEquals(date, result.getTrainingDate());
        assertEquals(45, result.getTrainingDuration());
    }

    @Test
    void selectTraining_shouldReturnTrainingWhenExists() {
        Training training = new Training();
        training.setId(1L);
        training.setTrainingName("Morning Fitness");
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.selectTraining(1L);

        assertTrue(result.isPresent());
        assertEquals("Morning Fitness", result.get().getTrainingName());
    }

    @Test
    void selectTraining_shouldReturnEmptyWhenNotExists() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Training> result = trainingService.selectTraining(999L);

        assertFalse(result.isPresent());
    }
}
