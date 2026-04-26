package com.gymconnect.storage;

import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerTest {

    private StorageInitializer storageInitializer;
    private ConcurrentHashMap<Long, Trainee> traineeStorage;
    private ConcurrentHashMap<Long, Trainer> trainerStorage;
    private ConcurrentHashMap<Long, Training> trainingStorage;

    @BeforeEach
    void setUp() {
        storageInitializer = new StorageInitializer();
        traineeStorage = new ConcurrentHashMap<>();
        trainerStorage = new ConcurrentHashMap<>();
        trainingStorage = new ConcurrentHashMap<>();
        storageInitializer.setTraineeStorage(traineeStorage);
        storageInitializer.setTrainerStorage(trainerStorage);
        storageInitializer.setTrainingStorage(trainingStorage);
    }

    @Test
    void afterPropertiesSet_shouldLoadDataFromJsonFile() {
        storageInitializer.setDataFilePath("initial-data.json");

        storageInitializer.afterPropertiesSet();

        assertEquals(2, traineeStorage.size());
        assertEquals(2, trainerStorage.size());
        assertEquals(2, trainingStorage.size());
    }

    @Test
    void afterPropertiesSet_shouldLoadTraineeDataCorrectly() {
        storageInitializer.setDataFilePath("initial-data.json");

        storageInitializer.afterPropertiesSet();

        Trainee trainee = traineeStorage.get(1L);
        assertNotNull(trainee);
        assertEquals("John", trainee.getFirstName());
        assertEquals("Smith", trainee.getLastName());
        assertEquals("John.Smith", trainee.getUsername());
    }

    @Test
    void afterPropertiesSet_shouldLoadTrainerDataCorrectly() {
        storageInitializer.setDataFilePath("initial-data.json");

        storageInitializer.afterPropertiesSet();

        Trainer trainer = trainerStorage.get(1L);
        assertNotNull(trainer);
        assertEquals("Mike", trainer.getFirstName());
        assertEquals("Johnson", trainer.getLastName());
    }

    @Test
    void afterPropertiesSet_shouldLoadTrainingDataCorrectly() {
        storageInitializer.setDataFilePath("initial-data.json");

        storageInitializer.afterPropertiesSet();

        Training training = trainingStorage.get(1L);
        assertNotNull(training);
        assertEquals("Morning Fitness Session", training.getTrainingName());
    }

    @Test
    void afterPropertiesSet_shouldHandleMissingFile() {
        storageInitializer.setDataFilePath("non-existent.json");

        storageInitializer.afterPropertiesSet();

        assertTrue(traineeStorage.isEmpty());
        assertTrue(trainerStorage.isEmpty());
        assertTrue(trainingStorage.isEmpty());
    }
}
