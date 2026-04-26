package com.gymconnect.dao;

import com.gymconnect.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoTest {

    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        traineeDao = new TraineeDao();
        traineeDao.setStorage(new ConcurrentHashMap<>());
    }

    @Test
    void save_shouldAssignIdAndStoreTrainee() {
        Trainee trainee = createTrainee("John", "Smith");

        Trainee saved = traineeDao.save(trainee);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("John", saved.getFirstName());
    }

    @Test
    void save_shouldIncrementIdForMultipleTrainees() {
        Trainee trainee1 = createTrainee("John", "Smith");
        Trainee trainee2 = createTrainee("Jane", "Doe");

        traineeDao.save(trainee1);
        traineeDao.save(trainee2);

        assertEquals(1L, trainee1.getId());
        assertEquals(2L, trainee2.getId());
    }

    @Test
    void findById_shouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee("John", "Smith");
        traineeDao.save(trainee);

        Optional<Trainee> found = traineeDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotExists() {
        Optional<Trainee> found = traineeDao.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_shouldReturnAllTrainees() {
        traineeDao.save(createTrainee("John", "Smith"));
        traineeDao.save(createTrainee("Jane", "Doe"));

        List<Trainee> all = traineeDao.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoTrainees() {
        List<Trainee> all = traineeDao.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    void update_shouldUpdateExistingTrainee() {
        Trainee trainee = createTrainee("John", "Smith");
        traineeDao.save(trainee);

        trainee.setFirstName("Johnny");
        Trainee updated = traineeDao.update(trainee);

        assertNotNull(updated);
        assertEquals("Johnny", updated.getFirstName());
    }

    @Test
    void update_shouldReturnNullWhenTraineeNotExists() {
        Trainee trainee = createTrainee("John", "Smith");
        trainee.setId(999L);

        Trainee updated = traineeDao.update(trainee);

        assertNull(updated);
    }

    @Test
    void delete_shouldRemoveTraineeAndReturnTrue() {
        Trainee trainee = createTrainee("John", "Smith");
        traineeDao.save(trainee);

        boolean deleted = traineeDao.delete(1L);

        assertTrue(deleted);
        assertFalse(traineeDao.findById(1L).isPresent());
    }

    @Test
    void delete_shouldReturnFalseWhenTraineeNotExists() {
        boolean deleted = traineeDao.delete(999L);

        assertFalse(deleted);
    }

    @Test
    void afterPropertiesSet_shouldInitializeIdCounterFromStorage() {
        ConcurrentHashMap<Long, Trainee> storage = new ConcurrentHashMap<>();
        Trainee existing = createTrainee("John", "Smith");
        existing.setId(5L);
        storage.put(5L, existing);

        traineeDao.setStorage(storage);
        traineeDao.afterPropertiesSet();

        Trainee newTrainee = createTrainee("Jane", "Doe");
        traineeDao.save(newTrainee);

        assertEquals(6L, newTrainee.getId());
    }

    private Trainee createTrainee(String firstName, String lastName) {
        return new Trainee(firstName, lastName, true, LocalDate.of(1995, 6, 15), "123 Main St");
    }
}
