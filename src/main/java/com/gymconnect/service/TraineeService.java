package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        logger.info("Creating trainee profile for: {} {}", firstName, lastName);

        List<String> existingUsernames = getAllUsernames();
        String username = usernameGenerator.generateUsername(firstName, lastName, existingUsernames);
        String password = passwordGenerator.generatePassword();

        Trainee trainee = new Trainee(firstName, lastName, true, dateOfBirth, address);
        trainee.setUsername(username);
        trainee.setPassword(password);

        Trainee saved = traineeDao.save(trainee);
        logger.info("Trainee profile created with id: {} and username: {}", saved.getId(), saved.getUsername());
        return saved;
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Updating trainee profile with id: {}", trainee.getId());
        Trainee updated = traineeDao.update(trainee);
        if (updated == null) {
            logger.warn("Failed to update trainee: not found with id: {}", trainee.getId());
        } else {
            logger.info("Trainee profile updated successfully for id: {}", updated.getId());
        }
        return updated;
    }

    public boolean deleteTrainee(Long id) {
        logger.info("Deleting trainee profile with id: {}", id);
        boolean deleted = traineeDao.delete(id);
        if (!deleted) {
            logger.warn("Failed to delete trainee: not found with id: {}", id);
        } else {
            logger.info("Trainee profile deleted successfully for id: {}", id);
        }
        return deleted;
    }

    public Optional<Trainee> selectTrainee(Long id) {
        logger.debug("Selecting trainee profile with id: {}", id);
        return traineeDao.findById(id);
    }

    private List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        usernames.addAll(traineeDao.findAll().stream()
                .map(t -> t.getUsername())
                .collect(Collectors.toList()));
        usernames.addAll(trainerDao.findAll().stream()
                .map(t -> t.getUsername())
                .collect(Collectors.toList()));
        return usernames;
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
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }
}
