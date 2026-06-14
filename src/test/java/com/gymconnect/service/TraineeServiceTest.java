package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.dao.TrainingDao;
import com.gymconnect.dao.UserDao;
import com.gymconnect.metrics.GymMetrics;
import com.gymconnect.model.Trainee;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

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
    private TraineeService traineeService;

    private User user;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", true);
        user.setId(1L);
        user.setUsername("John.Doe");
        user.setPassword("testPass123");

        trainee = new Trainee(user, LocalDate.of(1995, 6, 15), "123 Main St");
        trainee.setId(1L);
    }

    @Test
    void createTrainee_shouldCreateSuccessfully() {
        when(userDao.findAllUsernames()).thenReturn(new ArrayList<>());
        when(usernameGenerator.generateUsername("John", "Doe", new ArrayList<>()))
                .thenReturn("John.Doe");
        when(passwordGenerator.generatePassword()).thenReturn("testPass123");
        when(passwordEncoder.encode("testPass123")).thenReturn("$2a$hash");
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> {
            Trainee t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee result = traineeService.createTrainee("John", "Doe",
                LocalDate.of(1995, 6, 15), "123 Main St");

        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("testPass123", result.getUser().getRawPassword());
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void createTrainee_shouldThrow_whenFirstNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.createTrainee(null, "Doe", null, null));
    }

    @Test
    void createTrainee_shouldThrow_whenLastNameBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.createTrainee("John", "  ", null, null));
    }

    @Test
    void authenticate_shouldReturnTrue_whenCredentialsMatch() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("testPass123", "testPass123")).thenReturn(true);

        boolean result = traineeService.authenticate("John.Doe", "testPass123");

        assertTrue(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenPasswordWrong() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(passwordEncoder.matches("wrongPass", "testPass123")).thenReturn(false);

        boolean result = traineeService.authenticate("John.Doe", "wrongPass");

        assertFalse(result);
    }

    @Test
    void authenticate_shouldReturnFalse_whenUserNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        boolean result = traineeService.authenticate("unknown", "pass");

        assertFalse(result);
    }

    @Test
    void getTraineeByUsername_shouldReturnTrainee() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.getTraineeByUsername("John.Doe");

        assertTrue(result.isPresent());
        assertEquals("John.Doe", result.get().getUser().getUsername());
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(trainee);
        when(passwordEncoder.encode("newPass1234")).thenReturn("$2a$hashednew");

        traineeService.changePassword("John.Doe", "newPass1234");

        assertEquals("$2a$hashednew", trainee.getUser().getPassword());
        verify(traineeDao).update(trainee);
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.changePassword("John.Doe", ""));
    }

    @Test
    void changePassword_shouldThrow_whenTraineeNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.changePassword("unknown", "newPass"));
    }

    @Test
    void updateTrainee_shouldUpdateAllFields() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateTrainee("John.Doe", "Johnny", "Doe",
                LocalDate.of(1995, 6, 15), "New Address", true);

        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("New Address", trainee.getAddress());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTrainee_shouldThrow_whenFirstNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainee("John.Doe", null, "Doe",
                        null, null, true));
    }

    @Test
    void activateTrainee_shouldActivate_whenInactive() {
        user.setIsActive(false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(trainee);

        traineeService.activateTrainee("John.Doe");

        assertTrue(trainee.getUser().getIsActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void activateTrainee_shouldThrow_whenAlreadyActive() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class,
                () -> traineeService.activateTrainee("John.Doe"));
    }

    @Test
    void activateTrainee_shouldThrow_whenNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.activateTrainee("unknown"));
    }

    @Test
    void deactivateTrainee_shouldDeactivate_whenActive() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(traineeDao.update(any(Trainee.class))).thenReturn(trainee);

        traineeService.deactivateTrainee("John.Doe");

        assertFalse(trainee.getUser().getIsActive());
        verify(traineeDao).update(trainee);
    }

    @Test
    void deactivateTrainee_shouldThrow_whenAlreadyInactive() {
        user.setIsActive(false);
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

        assertThrows(IllegalStateException.class,
                () -> traineeService.deactivateTrainee("John.Doe"));
    }

    @Test
    void deleteTraineeByUsername_shouldCallDao() {
        traineeService.deleteTraineeByUsername("John.Doe");

        verify(traineeDao).deleteByUsername("John.Doe");
    }

    @Test
    void getTraineeTrainings_shouldDelegateToDaoWithCriteria() {
        List<Training> trainings = List.of(new Training());
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(trainingDao.findTraineeTrainings("John.Doe", from, to, "trainer", "FITNESS"))
                .thenReturn(trainings);

        List<Training> result = traineeService.getTraineeTrainings(
                "John.Doe", from, to, "trainer", "FITNESS");

        assertEquals(1, result.size());
    }

    @Test
    void getUnassignedTrainers_shouldDelegateToDao() {
        TrainingType type = new TrainingType("FITNESS");
        User trainerUser = new User("Mike", "J", true);
        trainerUser.setUsername("Mike.J");
        trainerUser.setPassword("pass");
        List<Trainer> trainers = List.of(new Trainer(trainerUser, type));
        when(trainerDao.findUnassignedTrainers("John.Doe")).thenReturn(trainers);

        List<Trainer> result = traineeService.getUnassignedTrainers("John.Doe");

        assertEquals(1, result.size());
    }

    @Test
    void updateTraineeTrainers_shouldUpdateTrainersList() {
        TrainingType type = new TrainingType("FITNESS");
        User trainerUser = new User("Mike", "J", true);
        trainerUser.setUsername("Mike.J");
        trainerUser.setPassword("pass");
        Trainer trainer = new Trainer(trainerUser, type);

        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("Mike.J")).thenReturn(Optional.of(trainer));
        when(traineeDao.update(any(Trainee.class))).thenReturn(trainee);

        traineeService.updateTraineeTrainers("John.Doe", List.of("Mike.J"));

        assertEquals(1, trainee.getTrainers().size());
        verify(traineeDao).update(trainee);
    }

    @Test
    void updateTraineeTrainers_shouldThrow_whenTraineeNotFound() {
        when(traineeDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTraineeTrainers("unknown", List.of("Mike.J")));
    }

    @Test
    void updateTraineeTrainers_shouldThrow_whenTrainerNotFound() {
        when(traineeDao.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTraineeTrainers("John.Doe", List.of("unknown")));
    }
}
