package com.gymconnect.dao;

import com.gymconnect.model.Trainer;
import com.gymconnect.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        trainerDao = new TrainerDao();
        trainerDao.setStorage(new ConcurrentHashMap<>());
    }

    @Test
    void save_shouldAssignIdAndStoreTrainer() {
        Trainer trainer = createTrainer("Mike", "Johnson");

        Trainer saved = trainerDao.save(trainer);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Mike", saved.getFirstName());
    }

    @Test
    void save_shouldIncrementIdForMultipleTrainers() {
        Trainer trainer1 = createTrainer("Mike", "Johnson");
        Trainer trainer2 = createTrainer("Sarah", "Williams");

        trainerDao.save(trainer1);
        trainerDao.save(trainer2);

        assertEquals(1L, trainer1.getId());
        assertEquals(2L, trainer2.getId());
    }

    @Test
    void findById_shouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer("Mike", "Johnson");
        trainerDao.save(trainer);

        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Mike", found.get().getFirstName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Trainer> found = trainerDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_shouldReturnAllTrainers() {
        trainerDao.save(createTrainer("Mike", "Johnson"));
        trainerDao.save(createTrainer("Sarah", "Williams"));

        List<Trainer> all = trainerDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoTrainers() {
        List<Trainer> all = trainerDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void update_shouldUpdateExistingTrainer() {
        Trainer trainer = createTrainer("Mike", "Johnson");
        trainerDao.save(trainer);

        trainer.setSpecialization(TrainingType.YOGA);
        Trainer updated = trainerDao.update(trainer);

        assertNotNull(updated);
        assertEquals(TrainingType.YOGA, updated.getSpecialization());
    }

    @Test
    void update_shouldReturnNullWhenTrainerNotExists() {
        Trainer trainer = createTrainer("Mike", "Johnson");
        trainer.setId(999L);

        Trainer updated = trainerDao.update(trainer);

        assertNull(updated);
    }

    @Test
    void afterPropertiesSet_shouldInitializeIdCounterFromStorage() {
        ConcurrentHashMap<Long, Trainer> storage = new ConcurrentHashMap<>();
        Trainer existing = createTrainer("Mike", "Johnson");
        existing.setId(5L);
        storage.put(5L, existing);

        trainerDao.setStorage(storage);
        trainerDao.afterPropertiesSet();

        Trainer newTrainer = createTrainer("Sarah", "Williams");
        trainerDao.save(newTrainer);

        assertEquals(6L, newTrainer.getId());
    }

    private Trainer createTrainer(String firstName, String lastName) {
        return new Trainer(firstName, lastName, true, TrainingType.FITNESS);
    }
}
