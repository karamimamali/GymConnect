package com.gymconnect.service;

import com.gymconnect.dao.TraineeDao;
import com.gymconnect.dao.TrainerDao;
import com.gymconnect.dao.TrainingDao;
import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

    @Transactional
    public Training addTraining(String traineeUsername, String trainerUsername,
                                 String trainingName, String trainingTypeName,
                                 LocalDate trainingDate, Integer trainingDuration) {
        logger.info("Adding training '{}' for trainee: {} with trainer: {}",
                trainingName, traineeUsername, trainerUsername);

        validateRequired(traineeUsername, "Trainee username");
        validateRequired(trainerUsername, "Trainer username");
        validateRequired(trainingName, "Training name");
        validateRequired(trainingTypeName, "Training type");
        if (trainingDate == null) {
            throw new IllegalArgumentException("Training date is required");
        }
        if (trainingDuration == null) {
            throw new IllegalArgumentException("Training duration is required");
        }

        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainee not found: " + traineeUsername));
        Trainer trainer = trainerDao.findByUsername(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found: " + trainerUsername));
        TrainingType trainingType = trainingTypeDao.findByName(trainingTypeName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Training type not found: " + trainingTypeName));

        Training training = new Training(trainee, trainer, trainingName,
                trainingType, trainingDate, trainingDuration);
        Training saved = trainingDao.save(training);
        logger.info("Training created with id: {}", saved.getId());
        return saved;
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
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
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }
}
