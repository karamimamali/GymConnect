package com.gymconnect.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerTest {

    @Test
    void defaultConstructor_shouldCreateEmptyTrainer() {
        Trainer trainer = new Trainer();

        assertNull(trainer.getId());
        assertNull(trainer.getUser());
        assertNull(trainer.getSpecialization());
        assertNotNull(trainer.getTrainees());
        assertNotNull(trainer.getTrainings());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        User user = new User("Mike", "Johnson", true);
        TrainingType type = new TrainingType("FITNESS");

        Trainer trainer = new Trainer(user, type);

        assertEquals(user, trainer.getUser());
        assertEquals(type, trainer.getSpecialization());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Trainer trainer = new Trainer();
        User user = new User("Mike", "Johnson", true);
        TrainingType type = new TrainingType("FITNESS");
        List<Trainee> trainees = new ArrayList<>();
        List<Training> trainings = new ArrayList<>();

        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization(type);
        trainer.setTrainees(trainees);
        trainer.setTrainings(trainings);

        assertEquals(1L, trainer.getId());
        assertEquals(user, trainer.getUser());
        assertEquals(type, trainer.getSpecialization());
        assertEquals(trainees, trainer.getTrainees());
        assertEquals(trainings, trainer.getTrainings());
    }

    @Test
    void equals_shouldReturnTrue_whenSameId() {
        Trainer t1 = new Trainer();
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(1L);

        assertEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        Trainer t1 = new Trainer();
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(2L);

        assertNotEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenNull() {
        Trainer t = new Trainer();
        t.setId(1L);

        assertNotEquals(null, t);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        Trainer t = new Trainer();
        t.setId(1L);

        assertNotEquals("string", t);
    }

    @Test
    void hashCode_shouldBeEqual_whenSameId() {
        Trainer t1 = new Trainer();
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(1L);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_shouldContainFields() {
        User user = new User("Mike", "Johnson", true);
        user.setId(1L);
        user.setUsername("Mike.Johnson");
        TrainingType type = new TrainingType("FITNESS");
        Trainer trainer = new Trainer(user, type);
        trainer.setId(1L);

        String result = trainer.toString();

        assertTrue(result.contains("Mike"));
        assertTrue(result.contains("FITNESS"));
    }
}
