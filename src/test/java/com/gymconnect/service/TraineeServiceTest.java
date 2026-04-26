package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createTrainee_shouldGenerateUsernameAndPassword() {
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(usernameGenerator.generateUsername(eq("John"), eq("Smith"), anyList())).thenReturn("John.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("aB3dEfGh1j");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee result = traineeService.createTrainee("John", "Smith",
                LocalDate.of(1995, 6, 15), "123 Main St");

        assertNotNull(result);
        assertEquals("John.Smith", result.getUsername());
        assertEquals("aB3dEfGh1j", result.getPassword());
        assertEquals("John", result.getFirstName());
        assertTrue(result.isActive());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void createTrainee_shouldCheckExistingUsernamesFromBothDaos() {
        Trainee existingTrainee = new Trainee();
        existingTrainee.setUsername("John.Smith");
        Trainer existingTrainer = new Trainer();
        existingTrainer.setUsername("Mike.Johnson");

        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));
        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        when(usernameGenerator.generateUsername(eq("John"), eq("Smith"), anyList())).thenReturn("John.Smith1");
        when(passwordGenerator.generatePassword()).thenReturn("xY9zAbCd2e");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(2L);
            return t;
        });

        Trainee result = traineeService.createTrainee("John", "Smith",
                LocalDate.of(1998, 3, 22), "456 Oak Ave");

        assertEquals("John.Smith1", result.getUsername());
        verify(usernameGenerator).generateUsername(eq("John"), eq("Smith"), argThat(list ->
                list.contains("John.Smith") && list.contains("Mike.Johnson")));
    }

    @Test
    void updateTrainee_shouldDelegateToDao() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("Johnny");
        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.updateTrainee(trainee);

        assertNotNull(result);
        assertEquals("Johnny", result.getFirstName());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTrainee_shouldReturnNullWhenNotFound() {
        Trainee trainee = new Trainee();
        trainee.setId(999L);
        when(traineeDao.update(trainee)).thenReturn(null);

        Trainee result = traineeService.updateTrainee(trainee);

        assertNull(result);
    }

    @Test
    void deleteTrainee_shouldDelegateToDao() {
        when(traineeDao.delete(1L)).thenReturn(true);

        boolean result = traineeService.deleteTrainee(1L);

        assertTrue(result);
        verify(traineeDao).delete(1L);
    }

    @Test
    void deleteTrainee_shouldReturnFalseWhenNotFound() {
        when(traineeDao.delete(999L)).thenReturn(false);

        boolean result = traineeService.deleteTrainee(999L);

        assertFalse(result);
    }

    @Test
    void selectTrainee_shouldReturnTraineeWhenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.selectTrainee(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void selectTrainee_shouldReturnEmptyWhenNotExists() {
        when(traineeDao.findById(999L)).thenReturn(Optional.empty());

        Optional<Trainee> result = traineeService.selectTrainee(999L);

        assertFalse(result.isPresent());
    }
}
