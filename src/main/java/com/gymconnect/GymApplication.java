package com.gymconnect;

import com.gymconnect.config.AppConfig;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Optional;

public class GymApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymApplication.class);

    public static void main(String[] args) {
        logger.info("Starting GymConnect application");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            Trainee trainee = facade.createTrainee("Alice", "Brown",
                    LocalDate.of(2000, 1, 15), "789 Elm St, Capital City");
            logger.info("Created trainee: {}", trainee);

            Trainer trainer = facade.createTrainer("Bob", "Davis", TrainingType.RESISTANCE);
            logger.info("Created trainer: {}", trainer);

            Training training = facade.createTraining(trainee.getId(), trainer.getId(),
                    "Strength Training", TrainingType.RESISTANCE,
                    LocalDate.of(2026, 4, 28), 90);
            logger.info("Created training: {}", training);

            Optional<Trainee> selectedTrainee = facade.selectTrainee(trainee.getId());
            selectedTrainee.ifPresent(t -> logger.info("Selected trainee: {}", t));

            Optional<Trainer> selectedTrainer = facade.selectTrainer(trainer.getId());
            selectedTrainer.ifPresent(t -> logger.info("Selected trainer: {}", t));

            Optional<Training> selectedTraining = facade.selectTraining(training.getId());
            selectedTraining.ifPresent(t -> logger.info("Selected training: {}", t));

            logger.info("GymConnect application completed successfully");
        }
    }
}
