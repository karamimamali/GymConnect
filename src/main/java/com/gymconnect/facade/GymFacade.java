package com.gymconnect.facade;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import com.gymconnect.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // 1. Create Trainee profile (public - no auth required)
    public Trainee createTrainee(String firstName, String lastName,
                                  LocalDate dateOfBirth, String address) {
        logger.info("Facade: creating trainee profile");
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    // 2. Create Trainer profile (public - no auth required)
    public Trainer createTrainer(String firstName, String lastName,
                                  String specializationName) {
        logger.info("Facade: creating trainer profile");
        return trainerService.createTrainer(firstName, lastName, specializationName);
    }

    // 3. Trainee username/password matching (used for login and change-password)
    public boolean authenticateTrainee(String username, String password) {
        logger.debug("Facade: authenticating trainee");
        return traineeService.authenticate(username, password);
    }

    // 4. Trainer username/password matching (used for login and change-password)
    public boolean authenticateTrainer(String username, String password) {
        logger.debug("Facade: authenticating trainer");
        return trainerService.authenticate(username, password);
    }

    // 5. Change password - verifies old password, then stores new BCrypt hash
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Facade: changing password for user: {}", username);
        boolean isTrainee = traineeService.authenticate(username, oldPassword);
        if (isTrainee) {
            traineeService.changePassword(username, newPassword);
            return;
        }
        boolean isTrainer = trainerService.authenticate(username, oldPassword);
        if (isTrainer) {
            trainerService.changePassword(username, newPassword);
            return;
        }
        logger.warn("Facade: change-password failed - invalid old password for user: {}", username);
        throw new SecurityException("Invalid old password");
    }

    // 6. Select Trainer profile by username (JWT auth enforced by Spring Security)
    public Trainer getTrainerByUsername(String username) {
        logger.debug("Facade: selecting trainer profile by username");
        return trainerService.getTrainerByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    // 7. Select Trainee profile by username (JWT auth enforced by Spring Security)
    public Trainee getTraineeByUsername(String username) {
        logger.debug("Facade: selecting trainee profile by username");
        return traineeService.getTraineeByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
    }

    // 8. Update trainer profile (JWT auth enforced by Spring Security)
    public Trainer updateTrainer(String username, String firstName, String lastName,
                                  String specializationName, Boolean isActive) {
        logger.info("Facade: updating trainer profile");
        return trainerService.updateTrainer(username, firstName, lastName,
                specializationName, isActive);
    }

    // 9. Update trainee profile (JWT auth enforced by Spring Security)
    public Trainee updateTrainee(String username, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address, Boolean isActive) {
        logger.info("Facade: updating trainee profile");
        return traineeService.updateTrainee(username, firstName, lastName,
                dateOfBirth, address, isActive);
    }

    // 10. Activate trainee (JWT auth enforced by Spring Security)
    public void activateTrainee(String username) {
        logger.info("Facade: activating trainee");
        traineeService.activateTrainee(username);
    }

    // 11. Deactivate trainee (JWT auth enforced by Spring Security)
    public void deactivateTrainee(String username) {
        logger.info("Facade: deactivating trainee");
        traineeService.deactivateTrainee(username);
    }

    // 12. Activate trainer (JWT auth enforced by Spring Security)
    public void activateTrainer(String username) {
        logger.info("Facade: activating trainer");
        trainerService.activateTrainer(username);
    }

    // 13. Deactivate trainer (JWT auth enforced by Spring Security)
    public void deactivateTrainer(String username) {
        logger.info("Facade: deactivating trainer");
        trainerService.deactivateTrainer(username);
    }

    // 14. Delete trainee profile by username (JWT auth enforced by Spring Security)
    public void deleteTraineeByUsername(String username) {
        logger.info("Facade: deleting trainee profile");
        traineeService.deleteTraineeByUsername(username);
    }

    // 15. Get Trainee Trainings List (JWT auth enforced by Spring Security)
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate,
                                               LocalDate toDate, String trainerName,
                                               String trainingTypeName) {
        logger.debug("Facade: getting trainee trainings");
        return traineeService.getTraineeTrainings(username, fromDate, toDate,
                trainerName, trainingTypeName);
    }

    // 16. Get Trainer Trainings List (JWT auth enforced by Spring Security)
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate,
                                               LocalDate toDate, String traineeName) {
        logger.debug("Facade: getting trainer trainings");
        return trainerService.getTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    // 17. Add training (JWT auth enforced by Spring Security)
    public Training addTraining(String traineeUsername, String trainerUsername,
                                 String trainingName, LocalDate trainingDate,
                                 Integer trainingDuration) {
        logger.info("Facade: adding training");
        Trainer trainer = trainerService.getTrainerByUsername(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found: " + trainerUsername));
        String trainingTypeName = trainer.getSpecialization().getTrainingTypeName();
        return trainingService.addTraining(traineeUsername, trainerUsername,
                trainingName, trainingTypeName, trainingDate, trainingDuration);
    }

    // 18. Get trainers not assigned to trainee (JWT auth enforced by Spring Security)
    public List<Trainer> getUnassignedTrainers(String username) {
        logger.debug("Facade: getting unassigned trainers");
        return traineeService.getUnassignedTrainers(username);
    }

    // 19. Update Trainee's trainers list (JWT auth enforced by Spring Security)
    public void updateTraineeTrainers(String username, List<String> trainerUsernames) {
        logger.info("Facade: updating trainee's trainers list");
        traineeService.updateTraineeTrainers(username, trainerUsernames);
    }
}
