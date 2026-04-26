package com.gymconnect.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

    @Value("${storage.initialdata.filepath}")
    private String dataFilePath;

    private Map<Long, Trainee> traineeStorage;
    private Map<Long, Trainer> trainerStorage;
    private Map<Long, Training> trainingStorage;

    @Override
    public void afterPropertiesSet() {
        logger.info("Initializing storage from file: {}", dataFilePath);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(dataFilePath)) {
            if (inputStream == null) {
                logger.warn("Initial data file not found: {}. Starting with empty storage.", dataFilePath);
                return;
            }

            JsonNode root = mapper.readTree(inputStream);

            if (root.has("trainees")) {
                List<Trainee> trainees = mapper.convertValue(
                        root.get("trainees"), new TypeReference<List<Trainee>>() {});
                for (Trainee trainee : trainees) {
                    traineeStorage.put(trainee.getId(), trainee);
                }
                logger.info("Loaded {} trainee(s) into storage", trainees.size());
            }

            if (root.has("trainers")) {
                List<Trainer> trainers = mapper.convertValue(
                        root.get("trainers"), new TypeReference<List<Trainer>>() {});
                for (Trainer trainer : trainers) {
                    trainerStorage.put(trainer.getId(), trainer);
                }
                logger.info("Loaded {} trainer(s) into storage", trainers.size());
            }

            if (root.has("trainings")) {
                List<Training> trainings = mapper.convertValue(
                        root.get("trainings"), new TypeReference<List<Training>>() {});
                for (Training training : trainings) {
                    trainingStorage.put(training.getId(), training);
                }
                logger.info("Loaded {} training(s) into storage", trainings.size());
            }

        } catch (Exception e) {
            logger.error("Failed to initialize storage from file: {}", dataFilePath, e);
        }
    }

    @Autowired
    @Qualifier("traineeStorage")
    public void setTraineeStorage(Map<Long, Trainee> traineeStorage) {
        this.traineeStorage = traineeStorage;
    }

    @Autowired
    @Qualifier("trainerStorage")
    public void setTrainerStorage(Map<Long, Trainer> trainerStorage) {
        this.trainerStorage = trainerStorage;
    }

    @Autowired
    @Qualifier("trainingStorage")
    public void setTrainingStorage(Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }
}
