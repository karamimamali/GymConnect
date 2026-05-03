package com.gymconnect;

import com.gymconnect.config.AppConfig;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymApplication.class);

    public static void main(String[] args) {
        logger.info("Starting GymConnect application");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            // 1. Create Trainee profile
            Trainee trainee = facade.createTrainee("Alice", "Brown",
                    LocalDate.of(2000, 1, 15), "789 Elm St, Capital City");
            String traineePassword = trainee.getUser().getPassword();
            logger.info("Created trainee: {}", trainee.getUser().getUsername());

            // 2. Create Trainer profile
            Trainer trainer = facade.createTrainer("Bob", "Davis", "FITNESS");
            String trainerPassword = trainer.getUser().getPassword();
            logger.info("Created trainer: {}", trainer.getUser().getUsername());

            // 3. Trainee authentication
            boolean traineeAuth = facade.authenticateTrainee(
                    trainee.getUser().getUsername(), traineePassword);
            logger.info("Trainee authentication: {}", traineeAuth);

            // 4. Trainer authentication
            boolean trainerAuth = facade.authenticateTrainer(
                    trainer.getUser().getUsername(), trainerPassword);
            logger.info("Trainer authentication: {}", trainerAuth);

            // 5-6. Select profiles by username
            Trainee selectedTrainee = facade.getTraineeByUsername(
                    trainee.getUser().getUsername(), traineePassword);
            logger.info("Selected trainee: {}", selectedTrainee);

            Trainer selectedTrainer = facade.getTrainerByUsername(
                    trainer.getUser().getUsername(), trainerPassword);
            logger.info("Selected trainer: {}", selectedTrainer);

            // 16. Add training
            Training training = facade.addTraining(
                    trainee.getUser().getUsername(), traineePassword,
                    trainer.getUser().getUsername(),
                    "Morning Fitness", "FITNESS",
                    LocalDate.of(2026, 5, 1), 60);
            logger.info("Created training: {}", training);

            // 14. Get trainee trainings
            List<Training> traineeTrainings = facade.getTraineeTrainings(
                    trainee.getUser().getUsername(), traineePassword,
                    null, null, null, null);
            logger.info("Trainee trainings count: {}", traineeTrainings.size());

            logger.info("GymConnect application completed successfully");
        }
    }
}
