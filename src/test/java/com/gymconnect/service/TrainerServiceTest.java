package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createTrainer_shouldGenerateUsernameAndPassword() {
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generateUsername(eq("Mike"), eq("Johnson"), anyList())).thenReturn("Mike.Johnson");
        when(passwordGenerator.generatePassword()).thenReturn("sT7uVwXy3z");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer result = trainerService.createTrainer("Mike", "Johnson", TrainingType.FITNESS);

        assertNotNull(result);
        assertEquals("Mike.Johnson", result.getUsername());
        assertEquals("sT7uVwXy3z", result.getPassword());
        assertEquals(TrainingType.FITNESS, result.getSpecialization());
        assertTrue(result.isActive());
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void createTrainer_shouldCheckExistingUsernamesFromBothDaos() {
        Trainer existingTrainer = new Trainer();
        existingTrainer.setUsername("Mike.Johnson");
        Trainee existingTrainee = new Trainee();
        existingTrainee.setUsername("John.Smith");

        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));
        when(usernameGenerator.generateUsername(eq("Mike"), eq("Johnson"), anyList())).thenReturn("Mike.Johnson1");
        when(passwordGenerator.generatePassword()).thenReturn("xY9zAbCd2e");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        Trainer result = trainerService.createTrainer("Mike", "Johnson", TrainingType.YOGA);

        assertEquals("Mike.Johnson1", result.getUsername());
        verify(usernameGenerator).generateUsername(eq("Mike"), eq("Johnson"), argThat(list ->
                list.contains("Mike.Johnson") && list.contains("John.Smith")));
    }

    @Test
    void updateTrainer_shouldDelegateToDao() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setSpecialization(TrainingType.YOGA);
        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer result = trainerService.updateTrainer(trainer);

        assertNotNull(result);
        assertEquals(TrainingType.YOGA, result.getSpecialization());
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateTrainer_shouldReturnNullWhenNotFound() {
        Trainer trainer = new Trainer();
        trainer.setId(999L);
        when(trainerDao.update(trainer)).thenReturn(null);

        Trainer result = trainerService.updateTrainer(trainer);

        assertNull(result);
    }

    @Test
    void selectTrainer_shouldReturnTrainerWhenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.selectTrainer(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void selectTrainer_shouldReturnEmptyWhenNotExists() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.selectTrainer(999L);

        assertFalse(result.isPresent());
    }
}
