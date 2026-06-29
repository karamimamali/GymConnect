package com.gymconnect.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void defaultConstructor_shouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getFirstName());
    }

    @Test
    void parameterizedConstructor_shouldSetFields() {
        User user = new User("John", "Doe", true);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertTrue(user.getIsActive());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("John.Doe");
        user.setPassword("pass123");
        user.setIsActive(true);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John.Doe", user.getUsername());
        assertEquals("pass123", user.getPassword());
        assertTrue(user.getIsActive());
    }

    @Test
    void equals_shouldReturnTrue_whenSameId() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(1L);

        assertEquals(user1, user2);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentId() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        assertNotEquals(user1, user2);
    }

    @Test
    void equals_shouldReturnFalse_whenComparedToNull() {
        User user = new User();
        user.setId(1L);

        assertNotEquals(null, user);
    }

    @Test
    void equals_shouldReturnFalse_whenDifferentClass() {
        User user = new User();
        user.setId(1L);

        assertNotEquals("string", user);
    }

    @Test
    void equals_shouldReturnTrue_whenSameInstance() {
        User user = new User();
        user.setId(1L);

        assertEquals(user, user);
    }

    @Test
    void hashCode_shouldBeEqual_whenSameId() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(1L);

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void toString_shouldContainFields() {
        User user = new User("John", "Doe", true);
        user.setId(1L);
        user.setUsername("John.Doe");

        String result = user.toString();

        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
        assertTrue(result.contains("John.Doe"));
    }
}
