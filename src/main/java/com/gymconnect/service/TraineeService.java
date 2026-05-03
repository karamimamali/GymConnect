package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.dao.TrainingDao;
import com.gymconnect.dao.UserDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.User;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingDao trainingDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Transactional
    public Trainee createTrainee(String firstName, String lastName,
                                  LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile for: {} {}", firstName, lastName);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        List<String> existingUsernames = userDao.findAllUsernames();
        String username = usernameGenerator.generateUsername(firstName, lastName, existingUsernames);
        String password = passwordGenerator.generatePassword();

        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword(password);

        Trainee trainee = new Trainee(user, dateOfBirth, address);
        Trainee saved = traineeDao.save(trainee);
        logger.info("Trainee profile created with username: {}", username);
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.debug("Authenticating trainee with username: {}", username);
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        boolean authenticated = trainee.isPresent()
                && trainee.get().getUser().getPassword().equals(password);
        if (!authenticated) {
            logger.warn("Authentication failed for trainee: {}", username);
        }
        return authenticated;
    }

    @Transactional(readOnly = true)
    public Optional<Trainee> getTraineeByUsername(String username) {
        logger.debug("Selecting trainee profile by username: {}", username);
        return traineeDao.findByUsername(username);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for trainee: {}", username);
        validateRequired(newPassword, "New password");
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.getUser().setPassword(newPassword);
        traineeDao.update(trainee);
        logger.info("Password changed successfully for trainee: {}", username);
    }

    @Transactional
    public Trainee updateTrainee(String username, String firstName, String lastName,
                                  LocalDate dateOfBirth, String address, Boolean isActive) {
        logger.info("Updating trainee profile: {}", username);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.getUser().setIsActive(isActive);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee updated = traineeDao.update(trainee);
        logger.info("Trainee profile updated: {}", username);
        return updated;
    }

    @Transactional
    public void activateTrainee(String username) {
        logger.info("Activating trainee: {}", username);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        if (trainee.getUser().getIsActive()) {
            throw new IllegalStateException("Trainee is already active: " + username);
        }
        trainee.getUser().setIsActive(true);
        traineeDao.update(trainee);
        logger.info("Trainee activated: {}", username);
    }

    @Transactional
    public void deactivateTrainee(String username) {
        logger.info("Deactivating trainee: {}", username);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + username));
        if (!trainee.getUser().getIsActive()) {
            throw new IllegalStateException("Trainee is already inactive: " + username);
        }
        trainee.getUser().setIsActive(false);
        traineeDao.update(trainee);
        logger.info("Trainee deactivated: {}", username);
    }

    @Transactional
    public void deleteTraineeByUsername(String username) {
        logger.info("Deleting trainee profile: {}", username);
        traineeDao.deleteByUsername(username);
        logger.info("Trainee profile deleted: {}", username);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username, LocalDate fromDate,
                                               LocalDate toDate, String trainerName,
                                               String trainingTypeName) {
        logger.debug("Getting trainings for trainee: {}", username);
        return trainingDao.findTraineeTrainings(username, fromDate, toDate,
                trainerName, trainingTypeName);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        logger.debug("Getting unassigned trainers for trainee: {}", traineeUsername);
        return trainerDao.findUnassignedTrainers(traineeUsername);
    }

    @Transactional
    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        logger.info("Updating trainers list for trainee: {}", traineeUsername);
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee not found: " + traineeUsername));

        List<Trainer> trainers = new ArrayList<>();
        for (String trainerUsername : trainerUsernames) {
            Trainer trainer = trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Trainer not found: " + trainerUsername));
            trainers.add(trainer);
        }
        trainee.setTrainers(trainers);
        traineeDao.update(trainee);
        logger.info("Trainers list updated for trainee: {}", traineeUsername);
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
