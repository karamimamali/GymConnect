package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import com.gymconnect.util.PasswordGenerator;
import com.gymconnect.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        logger.info("Creating trainer profile for: {} {}", firstName, lastName);

        List<String> existingUsernames = getAllUsernames();
        String username = usernameGenerator.generateUsername(firstName, lastName, existingUsernames);
        String password = passwordGenerator.generatePassword();

        Trainer trainer = new Trainer(firstName, lastName, true, specialization);
        trainer.setUsername(username);
        trainer.setPassword(password);

        Trainer saved = trainerDao.save(trainer);
        logger.info("Trainer profile created with id: {} and username: {}", saved.getId(), saved.getUsername());
        return saved;
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Updating trainer profile with id: {}", trainer.getId());
        Trainer updated = trainerDao.update(trainer);
        if (updated == null) {
            logger.warn("Failed to update trainer: not found with id: {}", trainer.getId());
        } else {
            logger.info("Trainer profile updated successfully for id: {}", updated.getId());
        }
        return updated;
    }

    public Optional<Trainer> selectTrainer(Long id) {
        logger.debug("Selecting trainer profile with id: {}", id);
        return trainerDao.findById(id);
    }

    private List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        usernames.addAll(trainerDao.findAll().stream()
                .map(t -> t.getUsername())
                .collect(Collectors.toList()));
        usernames.addAll(traineeDao.findAll().stream()
                .map(t -> t.getUsername())
                .collect(Collectors.toList()));
        return usernames;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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
