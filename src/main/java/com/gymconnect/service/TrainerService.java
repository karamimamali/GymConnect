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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private TrainingDao trainingDao;
    private TrainingTypeDao trainingTypeDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private GymMetrics gymMetrics;

    @Transactional
    public Trainer createTrainer(String firstName, String lastName,
                                  String specializationName) {
        logger.info("Creating trainer profile for: {} {}", firstName, lastName);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");
        validateRequired(specializationName, "Specialization");

        TrainingType specialization = trainingTypeDao.findByName(specializationName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Training type not found: " + specializationName));

        List<String> existingUsernames = userDao.findAllUsernames();
        String username = usernameGenerator.generateUsername(firstName, lastName, existingUsernames);
        String password = passwordGenerator.generatePassword();

        User user = new User(firstName, lastName, true);
        user.setUsername(username);
        user.setPassword(password);

        Trainer trainer = new Trainer(user, specialization);
        Trainer saved = trainerDao.save(trainer);
        gymMetrics.recordTrainerRegistration();
        logger.info("Trainer profile created with username: {}", username);
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        logger.debug("Authenticating trainer with username: {}", username);
        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        boolean authenticated = trainer.isPresent()
                && trainer.get().getUser().getPassword().equals(password);
        if (authenticated) {
            gymMetrics.recordAuthenticationSuccess();
        } else {
            gymMetrics.recordAuthenticationFailure();
            logger.warn("Authentication failed for trainer: {}", username);
        }
        return authenticated;
    }

    @Transactional(readOnly = true)
    public Optional<Trainer> getTrainerByUsername(String username) {
        logger.debug("Selecting trainer profile by username: {}", username);
        return trainerDao.findByUsername(username);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        logger.info("Changing password for trainer: {}", username);
        validateRequired(newPassword, "New password");
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.getUser().setPassword(newPassword);
        trainerDao.update(trainer);
        logger.info("Password changed successfully for trainer: {}", username);
    }

    @Transactional
    public Trainer updateTrainer(String username, String firstName, String lastName,
                                  String specializationName, Boolean isActive) {
        logger.info("Updating trainer profile: {}", username);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");
        validateRequired(specializationName, "Specialization");

        TrainingType specialization = trainingTypeDao.findByName(specializationName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Training type not found: " + specializationName));

        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.getUser().setIsActive(isActive);
        trainer.setSpecialization(specialization);

        Trainer updated = trainerDao.update(trainer);
        logger.info("Trainer profile updated: {}", username);
        return updated;
    }

    @Transactional
    public void activateTrainer(String username) {
        logger.info("Activating trainer: {}", username);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        if (trainer.getUser().getIsActive()) {
            throw new IllegalStateException("Trainer is already active: " + username);
        }
        trainer.getUser().setIsActive(true);
        trainerDao.update(trainer);
        logger.info("Trainer activated: {}", username);
    }

    @Transactional
    public void deactivateTrainer(String username) {
        logger.info("Deactivating trainer: {}", username);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + username));
        if (!trainer.getUser().getIsActive()) {
            throw new IllegalStateException("Trainer is already inactive: " + username);
        }
        trainer.getUser().setIsActive(false);
        trainerDao.update(trainer);
        logger.info("Trainer deactivated: {}", username);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username, LocalDate fromDate,
                                               LocalDate toDate, String traineeName) {
        logger.debug("Getting trainings for trainer: {}", username);
        return trainingDao.findTrainerTrainings(username, fromDate, toDate, traineeName);
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
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
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
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

    @Autowired
    public void setGymMetrics(GymMetrics gymMetrics) {
        this.gymMetrics = gymMetrics;
    }
}
