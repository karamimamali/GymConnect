package com.gymconnect.security;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.User;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GymUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(GymUserDetailsService.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public GymUserDetailsService(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username: {}", username);

        Optional<Trainee> trainee = traineeService.getTraineeByUsername(username);
        if (trainee.isPresent()) {
            return toSpringUser(trainee.get().getUser());
        }

        Optional<Trainer> trainer = trainerService.getTrainerByUsername(username);
        if (trainer.isPresent()) {
            return toSpringUser(trainer.get().getUser());
        }

        logger.warn("User not found: {}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }

    private UserDetails toSpringUser(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .accountLocked(!user.getIsActive())
                .roles("USER")
                .build();
    }
}
