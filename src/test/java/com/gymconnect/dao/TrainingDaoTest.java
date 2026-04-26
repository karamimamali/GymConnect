package com.gymconnect.dao;

import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = new TrainingDao();
        trainingDao.setStorage(new ConcurrentHashMap<>());
    }

    @Test
    void save_shouldAssignIdAndStoreTraining() {
        Training training = createTraining("Morning Fitness");

        Training saved = trainingDao.save(training);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Morning Fitness", saved.getTrainingName());
    }

    @Test
    void save_shouldIncrementIdForMultipleTrainings() {
        Training training1 = createTraining("Morning Fitness");
        Training training2 = createTraining("Evening Yoga");

        trainingDao.save(training1);
        trainingDao.save(training2);

        assertEquals(1L, training1.getId());
        assertEquals(2L, training2.getId());
    }

    @Test
    void findById_shouldReturnTrainingWhenExists() {
        Training training = createTraining("Morning Fitness");
        trainingDao.save(training);

        Optional<Training> found = trainingDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Morning Fitness", found.get().getTrainingName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Training> found = trainingDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_shouldReturnAllTrainings() {
        trainingDao.save(createTraining("Morning Fitness"));
        trainingDao.save(createTraining("Evening Yoga"));

        List<Training> all = trainingDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoTrainings() {
        List<Training> all = trainingDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void afterPropertiesSet_shouldInitializeIdCounterFromStorage() {
        ConcurrentHashMap<Long, Training> storage = new ConcurrentHashMap<>();
        Training existing = createTraining("Existing Training");
        existing.setId(5L);
        storage.put(5L, existing);

        trainingDao.setStorage(storage);
        trainingDao.afterPropertiesSet();

        Training newTraining = createTraining("New Training");
        trainingDao.save(newTraining);

        assertEquals(6L, newTraining.getId());
    }

    private Training createTraining(String name) {
        return new Training(1L, 1L, name, TrainingType.FITNESS,
                LocalDate.of(2026, 4, 28), 60);
    }
}
