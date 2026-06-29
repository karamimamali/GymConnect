package com.gymconnect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeTest {

    @Test
    void defaultConstructor_shouldCreateEmptyTrainee() {
        Trainee trainee = new Trainee();

        assertNull(trainee.getId());
        assertNull(trainee.getUser());
        assertNotNull(trainee.getTrainers());
        assertNotNull(trainee.getTrainings());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        User user = new User("John", "Doe", true);
        LocalDate dob = LocalDate.of(1995, 6, 15);

        Trainee trainee = new Trainee(user, dob, "123 Main St");

        assertEquals(user, trainee.getUser());
        assertEquals(dob, trainee.getDateOfBirth());
        assertEquals("123 Main St", trainee.getAddress());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Trainee trainee = new Trainee();
        User user = new User("John", "Doe", true);
        LocalDate dob = LocalDate.of(1995, 6, 15);
        List<Trainer> trainers = new ArrayList<>();
        List<Training> trainings = new ArrayList<>();

        trainee.setId(1L);
        trainee.setUser(user);
        trainee.setDateOfBirth(dob);
        trainee.setAddress("456 Oak Ave");
        trainee.setTrainers(trainers);
        trainee.setTrainings(trainings);

        assertEquals(1L, trainee.getId());
        assertEquals(user, trainee.getUser());
        assertEquals(dob, trainee.getDateOfBirth());
        assertEquals("456 Oak Ave", trainee.getAddress());
        assertEquals(trainers, trainee.getTrainers());
        assertEquals(trainings, trainee.getTrainings());
    }

    @Test
    void equals_shouldReturnTrue_whenSameId() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        Trainee t2 = new Trainee();
        t2.setId(1L);

        assertEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        Trainee t2 = new Trainee();
        t2.setId(2L);

        assertNotEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenNull() {
        Trainee t = new Trainee();
        t.setId(1L);

        assertNotEquals(null, t);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        Trainee t = new Trainee();
        t.setId(1L);

        assertNotEquals("string", t);
    }

    @Test
    void hashCode_shouldBeEqual_whenSameId() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        Trainee t2 = new Trainee();
        t2.setId(1L);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_shouldContainFields() {
        User user = new User("John", "Doe", true);
        user.setId(1L);
        user.setUsername("John.Doe");
        Trainee trainee = new Trainee(user, LocalDate.of(1995, 6, 15), "123 Main St");
        trainee.setId(1L);

        String result = trainee.toString();

        assertTrue(result.contains("123 Main St"));
        assertTrue(result.contains("1995-06-15"));
    }
}
