package com.gymconnect.service;

import com.gymconnect.dao.TrainingDao;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private TrainingDao trainingDao;

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate,
                                   Number trainingDuration) {
        logger.info("Creating training: '{}' for traineeId: {} with trainerId: {}", trainingName, traineeId, trainerId);

        Training training = new Training(traineeId, trainerId, trainingName,
                trainingType, trainingDate, trainingDuration);

        Training saved = trainingDao.save(training);
        logger.info("Training created with id: {}", saved.getId());
        return saved;
    }

    public Optional<Training> selectTraining(Long id) {
        logger.debug("Selecting training with id: {}", id);
        return trainingDao.findById(id);
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }
}
