package com.gymconnect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTest {

    @Test
    void defaultConstructor_shouldCreateEmptyTraining() {
        Training training = new Training();

        assertNull(training.getId());
        assertNull(training.getTrainee());
        assertNull(training.getTrainer());
        assertNull(training.getTrainingName());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        User traineeUser = new User("John", "Doe", true);
        Trainee trainee = new Trainee(traineeUser, null, null);
        User trainerUser = new User("Mike", "Johnson", true);
        TrainingType type = new TrainingType("FITNESS");
        Trainer trainer = new Trainer(trainerUser, type);
        LocalDate date = LocalDate.of(2026, 5, 1);

        Training training = new Training(trainee, trainer, "Session", type, date, 60);

        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals("Session", training.getTrainingName());
        assertEquals(type, training.getTrainingType());
        assertEquals(date, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Training training = new Training();
        User traineeUser = new User("John", "Doe", true);
        Trainee trainee = new Trainee(traineeUser, null, null);
        User trainerUser = new User("Mike", "Johnson", true);
        TrainingType type = new TrainingType("FITNESS");
        Trainer trainer = new Trainer(trainerUser, type);
        LocalDate date = LocalDate.of(2026, 5, 1);

        training.setId(1L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Session");
        training.setTrainingType(type);
        training.setTrainingDate(date);
        training.setTrainingDuration(60);

        assertEquals(1L, training.getId());
        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals("Session", training.getTrainingName());
        assertEquals(type, training.getTrainingType());
        assertEquals(date, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void equals_shouldReturnTrue_whenSameId() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(1L);

        assertEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(2L);

        assertNotEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenNull() {
        Training t = new Training();
        t.setId(1L);

        assertNotEquals(null, t);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        Training t = new Training();
        t.setId(1L);

        assertNotEquals("string", t);
    }

    @Test
    void hashCode_shouldBeEqual_whenSameId() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(1L);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_shouldContainFields() {
        TrainingType type = new TrainingType("FITNESS");
        Training training = new Training();
        training.setId(1L);
        training.setTrainingName("Morning Session");
        training.setTrainingType(type);
        training.setTrainingDate(LocalDate.of(2026, 5, 1));
        training.setTrainingDuration(60);

        String result = training.toString();

        assertTrue(result.contains("Morning Session"));
        assertTrue(result.contains("60"));
    }
}
