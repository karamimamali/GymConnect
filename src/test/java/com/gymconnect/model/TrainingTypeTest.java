package com.gymconnect.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTypeTest {

    @Test
    void defaultConstructor_shouldCreateEmptyType() {
        TrainingType type = new TrainingType();

        assertNull(type.getId());
        assertNull(type.getTrainingTypeName());
    }

    @Test
    void parameterizedConstructor_shouldSetName() {
        TrainingType type = new TrainingType("FITNESS");

        assertEquals("FITNESS", type.getTrainingTypeName());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("YOGA");

        assertEquals(1L, type.getId());
        assertEquals("YOGA", type.getTrainingTypeName());
    }

    @Test
    void equals_shouldReturnTrue_whenSameId() {
        TrainingType t1 = new TrainingType();
        t1.setId(1L);
        TrainingType t2 = new TrainingType();
        t2.setId(1L);

        assertEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        TrainingType t1 = new TrainingType();
        t1.setId(1L);
        TrainingType t2 = new TrainingType();
        t2.setId(2L);

        assertNotEquals(t1, t2);
    }

    @Test
    void equals_shouldReturnFalse_whenNull() {
        TrainingType t = new TrainingType();
        t.setId(1L);

        assertNotEquals(null, t);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        TrainingType t = new TrainingType();
        t.setId(1L);

        assertNotEquals("string", t);
    }

    @Test
    void hashCode_shouldBeEqual_whenSameId() {
        TrainingType t1 = new TrainingType();
        t1.setId(1L);
        TrainingType t2 = new TrainingType();
        t2.setId(1L);

        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void toString_shouldContainName() {
        TrainingType type = new TrainingType("FITNESS");
        type.setId(1L);

        String result = type.toString();

        assertTrue(result.contains("FITNESS"));
    }
}
