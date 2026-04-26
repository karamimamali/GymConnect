package com.gymconnect.facade;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.service.TraineeService;
import com.gymconnect.service.TrainerService;
import com.gymconnect.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

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

    public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        logger.info("Facade: creating trainee profile");
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: updating trainee profile");
        return traineeService.updateTrainee(trainee);
    }

    public boolean deleteTrainee(Long id) {
        logger.info("Facade: deleting trainee profile");
        return traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> selectTrainee(Long id) {
        logger.debug("Facade: selecting trainee profile");
        return traineeService.selectTrainee(id);
    }

    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        logger.info("Facade: creating trainer profile");
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: updating trainer profile");
        return trainerService.updateTrainer(trainer);
    }

    public Optional<Trainer> selectTrainer(Long id) {
        logger.debug("Facade: selecting trainer profile");
        return trainerService.selectTrainer(id);
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                    TrainingType trainingType, LocalDate trainingDate,
                                    Number trainingDuration) {
        logger.info("Facade: creating training");
        return trainingService.createTraining(traineeId, trainerId, trainingName,
                trainingType, trainingDate, trainingDuration);
    }

    public Optional<Training> selectTraining(Long id) {
        logger.debug("Facade: selecting training");
        return trainingService.selectTraining(id);
    }
}
