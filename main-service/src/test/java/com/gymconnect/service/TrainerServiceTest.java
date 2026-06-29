package com.gymconnect.service;

import com.gymconnect.dao.TrainerDao;
import com.gymconnect.dao.TrainingDao;
import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.dao.UserDao;
import com.gymconnect.metrics.GymMetrics;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
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
class TrainerServiceTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService trainerService;

    private User user;
    private Trainer trainer;
    private TrainingType fitnessType;

    @BeforeEach
    void setUp() {
        fitnessType = new TrainingType("FITNESS");
        fitnessType.setId(1L);

        user = new User("Mike", "Johnson", true);
        user.setId(1L);
        user.setUsername("Mike.Johnson");
        user.setPassword("testPass123");

        trainer = new Trainer(user, fitnessType);
        trainer.setId(1L);
    }

    @Test
    void createTrainer_shouldCreateSuccessfully() {
        when(trainingTypeDao.findByName("FITNESS")).thenReturn(Optional.of(fitnessType));
        when(userDao.findAllUsernames()).thenReturn(new ArrayList<>());
        when(usernameGenerator.generateUsername("Mike", "Johnson", new ArrayList<>()))
                .thenReturn("Mike.Johnson");
        when(passwordGenerator.generatePassword()).thenReturn("testPass123");
        when(passwordEncoder.encode("testPass123")).thenReturn("$2a$hash");
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> {
            Trainer t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer result = trainerService.createTrainer("Mike", "Johnson", "FITNESS");

        assertNotNull(result);
        assertEquals("Mike.Johnson", result.getUser().getUsername());
        assertEquals("testPass123", result.getUser().getRawPassword());
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void createTrainer_shouldThrow_whenFirstNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainer(null, "Johnson", "FITNESS"));
    }

    @Test
    void createTrainer_shouldThrow_whenLastNameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainer("Mike", "", "FITNESS"));
    }

    @Test
    void createTrainer_shouldThrow_whenSpecializationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainer("Mike", "Johnson", null));
    }

    @Test
    void createTrainer_shouldThrow_whenSpecializationNotFound() {
        when(trainingTypeDao.findByName("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainer("Mike", "Johnson", "UNKNOWN"));
    }

    @Test
    void authenticate_shouldReturnTrue_whenCredentialsMatch() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("testPass123", "testPass123")).thenReturn(true);

        boolean result = trainerService.authenticate("Mike.Johnson", "testPass123");

        assertTrue(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenPasswordWrong() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(passwordEncoder.matches("wrongPass", "testPass123")).thenReturn(false);

        boolean result = trainerService.authenticate("Mike.Johnson", "wrongPass");

        assertFalse(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenUserNotFound() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = trainerService.authenticate("unknown", "pass");

        assertFalse(result);
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainer() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainerByUsername("Mike.Johnson");

        assertTrue(result.isPresent());
        assertEquals("Mike.Johnson", result.get().getUser().getUsername());
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(trainer);
        when(passwordEncoder.encode("newPass1234")).thenReturn("$2a$hashednew");

        trainerService.changePassword("Mike.Johnson", "newPass1234");

        assertEquals("$2a$hashednew", trainer.getUser().getPassword());
        verify(trainerDao).update(trainer);
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.changePassword("Mike.Johnson", ""));
    }

    @Test
    void changePassword_shouldThrow_whenTrainerNotFound() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.changePassword("unknown", "newPass"));
    }

    @Test
    void updateTrainer_shouldUpdateAllFields() {
        TrainingType yogaType = new TrainingType("YOGA");
        yogaType.setId(2L);
        when(trainingTypeDao.findByName("YOGA")).thenReturn(Optional.of(yogaType));
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.updateTrainer("Mike.Johnson", "Michael", "Johnson",
                "YOGA", true);

        assertEquals("Michael", trainer.getUser().getFirstName());
        assertEquals("YOGA", trainer.getSpecialization().getTrainingTypeName());
        verify(trainerDao).update(trainer);
    }

    @Test
    void updateTrainer_shouldThrow_whenFirstNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.updateTrainer("Mike.Johnson", null, "Johnson",
                        "FITNESS", true));
    }

    @Test
    void updateTrainer_shouldThrow_whenSpecializationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> trainerService.updateTrainer("Mike.Johnson", "Mike", "Johnson",
                        null, true));
    }

    @Test
    void activateTrainer_shouldActivate_whenInactive() {
        user.setIsActive(false);
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(trainer);

        trainerService.activateTrainer("Mike.Johnson");

        assertTrue(trainer.getUser().getIsActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void activateTrainer_shouldThrow_whenAlreadyActive() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class,
                () -> trainerService.activateTrainer("Mike.Johnson"));
    }

    @Test
    void activateTrainer_shouldThrow_whenNotFound() {
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> trainerService.activateTrainer("unknown"));
    }

    @Test
    void deactivateTrainer_shouldDeactivate_whenActive() {
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));
        when(trainerDao.update(any(Trainer.class))).thenReturn(trainer);

        trainerService.deactivateTrainer("Mike.Johnson");

        assertFalse(trainer.getUser().getIsActive());
        verify(trainerDao).update(trainer);
    }

    @Test
    void deactivateTrainer_shouldThrow_whenAlreadyInactive() {
        user.setIsActive(false);
        when(trainerDao.findByUsername("Mike.Johnson")).thenReturn(Optional.of(trainer));

        assertThrows(IllegalStateException.class,
                () -> trainerService.deactivateTrainer("Mike.Johnson"));
    }

    @Test
    void getTrainerTrainings_shouldDelegateToDaoWithCriteria() {
        List<Training> trainings = List.of(new Training());
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(trainingDao.findTrainerTrainings("Mike.Johnson", from, to, "trainee"))
                .thenReturn(trainings);

        List<Training> result = trainerService.getTrainerTrainings(
                "Mike.Johnson", from, to, "trainee");

        assertEquals(1, result.size());
    }
}
