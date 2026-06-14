package com.gymconnect.security;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    private GymUserDetailsService gymUserDetailsService;

    @BeforeEach
    void setUp() {
        gymUserDetailsService = new GymUserDetailsService(traineeService, trainerService);
    }

    @Test
    void loadUserByUsername_returnsUserDetails_whenTraineeFound() {
        User user = new User("Alice", "Brown", true);
        user.setUsername("Alice.Brown");
        user.setPassword("$2a$hashed");
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Address");

        when(traineeService.getTraineeByUsername("Alice.Brown")).thenReturn(Optional.of(trainee));

        UserDetails details = gymUserDetailsService.loadUserByUsername("Alice.Brown");

        assertEquals("Alice.Brown", details.getUsername());
        assertEquals("$2a$hashed", details.getPassword());
    }

    @Test
    void loadUserByUsername_returnsUserDetails_whenTrainerFound() {
        User user = new User("Bob", "Davis", true);
        user.setUsername("Bob.Davis");
        user.setPassword("$2a$hashed");
        Trainer trainer = new Trainer(user, new TrainingType("FITNESS"));

        when(traineeService.getTraineeByUsername("Bob.Davis")).thenReturn(Optional.empty());
        when(trainerService.getTrainerByUsername("Bob.Davis")).thenReturn(Optional.of(trainer));

        UserDetails details = gymUserDetailsService.loadUserByUsername("Bob.Davis");

        assertEquals("Bob.Davis", details.getUsername());
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenNotFound() {
        when(traineeService.getTraineeByUsername("unknown")).thenReturn(Optional.empty());
        when(trainerService.getTrainerByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> gymUserDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_accountLocked_whenUserInactive() {
        User user = new User("Alice", "Brown", false);
        user.setUsername("Alice.Brown");
        user.setPassword("$2a$hashed");
        Trainee trainee = new Trainee(user, LocalDate.of(2000, 1, 1), "Address");

        when(traineeService.getTraineeByUsername("Alice.Brown")).thenReturn(Optional.of(trainee));

        UserDetails details = gymUserDetailsService.loadUserByUsername("Alice.Brown");

        assertEquals(false, details.isAccountNonLocked());
    }
}
