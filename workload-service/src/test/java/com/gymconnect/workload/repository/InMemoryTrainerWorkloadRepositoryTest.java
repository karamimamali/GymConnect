package com.gymconnect.workload.repository;

import com.gymconnect.workload.model.TrainerWorkload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryTrainerWorkloadRepositoryTest {

    private InMemoryTrainerWorkloadRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTrainerWorkloadRepository();
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenAbsent() {
        assertTrue(repository.findByUsername("missing").isEmpty());
    }

    @Test
    void findOrCreate_shouldCreateAndStore_whenAbsent() {
        TrainerWorkload created = repository.findOrCreate("Mike.Johnson", "Mike", "Johnson", true);

        assertEquals("Mike.Johnson", created.getUsername());
        assertTrue(repository.findByUsername("Mike.Johnson").isPresent());
    }

    @Test
    void findOrCreate_shouldReturnExistingAndRefreshIdentity_whenPresent() {
        TrainerWorkload first = repository.findOrCreate("Mike.Johnson", "Mike", "Johnson", true);

        TrainerWorkload second = repository.findOrCreate("Mike.Johnson", "Michael", "Johnston", false);

        assertSame(first, second);
        assertEquals("Michael", second.getFirstName());
        assertFalse(second.isActive());
    }

    @Test
    void save_shouldPersistWorkload() {
        TrainerWorkload workload = new TrainerWorkload("Anna.Lee", "Anna", "Lee", true);

        repository.save(workload);

        Optional<TrainerWorkload> found = repository.findByUsername("Anna.Lee");
        assertTrue(found.isPresent());
        assertSame(workload, found.get());
    }
}
