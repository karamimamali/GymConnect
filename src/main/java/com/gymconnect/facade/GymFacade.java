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

    // 1. Create Trainee profile (no auth required)
    public Trainee createTrainee(String firstName, String lastName,
                                  LocalDate dateOfBirth, String address) {
        logger.info("Facade: creating trainee profile");
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    // 2. Create Trainer profile (no auth required)
    public Trainer createTrainer(String firstName, String lastName,
                                  String specializationName) {
        logger.info("Facade: creating trainer profile");
        return trainerService.createTrainer(firstName, lastName, specializationName);
    }

    // 3. Trainee username and password matching
    public boolean authenticateTrainee(String username, String password) {
        logger.debug("Facade: authenticating trainee");
        return traineeService.authenticate(username, password);
    }

    // 4. Trainer username and password matching
    public boolean authenticateTrainer(String username, String password) {
        logger.debug("Facade: authenticating trainer");
        return trainerService.authenticate(username, password);
    }

    // 5. Select Trainer profile by username (auth required)
    public Trainer getTrainerByUsername(String username, String password) {
        logger.debug("Facade: selecting trainer profile by username");
        authenticateTrainerOrFail(username, password);
        return trainerService.getTrainerByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
    }

    // 6. Select Trainee profile by username (auth required)
    public Trainee getTraineeByUsername(String username, String password) {
        logger.debug("Facade: selecting trainee profile by username");
        authenticateTraineeOrFail(username, password);
        return traineeService.getTraineeByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
    }

    // 7. Trainee password change (auth required)
    public void changeTraineePassword(String username, String oldPassword,
                                       String newPassword) {
        logger.info("Facade: changing trainee password");
        authenticateTraineeOrFail(username, oldPassword);
        traineeService.changePassword(username, newPassword);
    }

    // 8. Trainer password change (auth required)
    public void changeTrainerPassword(String username, String oldPassword,
                                       String newPassword) {
        logger.info("Facade: changing trainer password");
        authenticateTrainerOrFail(username, oldPassword);
        trainerService.changePassword(username, newPassword);
    }

    // 9. Update trainer profile (auth required)
    public Trainer updateTrainer(String username, String password, String firstName,
                                  String lastName, String specializationName,
                                  Boolean isActive) {
        logger.info("Facade: updating trainer profile");
        authenticateTrainerOrFail(username, password);
        return trainerService.updateTrainer(username, firstName, lastName,
                specializationName, isActive);
    }

    // 10. Update trainee profile (auth required)
    public Trainee updateTrainee(String username, String password, String firstName,
                                  String lastName, LocalDate dateOfBirth, String address,
                                  Boolean isActive) {
        logger.info("Facade: updating trainee profile");
        authenticateTraineeOrFail(username, password);
        return traineeService.updateTrainee(username, firstName, lastName,
                dateOfBirth, address, isActive);
    }

    // 11. Activate/De-activate trainee (auth required)
    public void activateTrainee(String username, String password) {
        logger.info("Facade: activating trainee");
        authenticateTraineeOrFail(username, password);
        traineeService.activateTrainee(username);
    }

    public void deactivateTrainee(String username, String password) {
        logger.info("Facade: deactivating trainee");
        authenticateTraineeOrFail(username, password);
        traineeService.deactivateTrainee(username);
    }

    // 12. Activate/De-activate trainer (auth required)
    public void activateTrainer(String username, String password) {
        logger.info("Facade: activating trainer");
        authenticateTrainerOrFail(username, password);
        trainerService.activateTrainer(username);
    }

    public void deactivateTrainer(String username, String password) {
        logger.info("Facade: deactivating trainer");
        authenticateTrainerOrFail(username, password);
        trainerService.deactivateTrainer(username);
    }

    // 13. Delete trainee profile by username (auth required)
    public void deleteTraineeByUsername(String username, String password) {
        logger.info("Facade: deleting trainee profile");
        authenticateTraineeOrFail(username, password);
        traineeService.deleteTraineeByUsername(username);
    }

    // 14. Get Trainee Trainings List (auth required)
    public List<Training> getTraineeTrainings(String username, String password,
                                               LocalDate fromDate, LocalDate toDate,
                                               String trainerName, String trainingTypeName) {
        logger.debug("Facade: getting trainee trainings");
        authenticateTraineeOrFail(username, password);
        return traineeService.getTraineeTrainings(username, fromDate, toDate,
                trainerName, trainingTypeName);
    }

    // 15. Get Trainer Trainings List (auth required)
    public List<Training> getTrainerTrainings(String username, String password,
                                               LocalDate fromDate, LocalDate toDate,
                                               String traineeName) {
        logger.debug("Facade: getting trainer trainings");
        authenticateTrainerOrFail(username, password);
        return trainerService.getTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    // 16. Add training (auth required — trainee authenticates)
    public Training addTraining(String traineeUsername, String traineePassword,
                                 String trainerUsername, String trainingName,
                                 String trainingTypeName, LocalDate trainingDate,
                                 Integer trainingDuration) {
        logger.info("Facade: adding training");
        authenticateTraineeOrFail(traineeUsername, traineePassword);
        return trainingService.addTraining(traineeUsername, trainerUsername,
                trainingName, trainingTypeName, trainingDate, trainingDuration);
    }

    // 17. Get trainers not assigned to trainee (auth required)
    public List<Trainer> getUnassignedTrainers(String username, String password) {
        logger.debug("Facade: getting unassigned trainers");
        authenticateTraineeOrFail(username, password);
        return traineeService.getUnassignedTrainers(username);
    }

    // 18. Update Trainee's trainers list (auth required)
    public void updateTraineeTrainers(String username, String password,
                                       List<String> trainerUsernames) {
        logger.info("Facade: updating trainee's trainers list");
        authenticateTraineeOrFail(username, password);
        traineeService.updateTraineeTrainers(username, trainerUsernames);
    }

    private void authenticateTraineeOrFail(String username, String password) {
        if (!traineeService.authenticate(username, password)) {
            logger.warn("Facade: trainee authentication failed for: {}", username);
            throw new SecurityException("Invalid trainee credentials");
        }
    }

    private void authenticateTrainerOrFail(String username, String password) {
        if (!trainerService.authenticate(username, password)) {
            logger.warn("Facade: trainer authentication failed for: {}", username);
            throw new SecurityException("Invalid trainer credentials");
        }
    }
}
