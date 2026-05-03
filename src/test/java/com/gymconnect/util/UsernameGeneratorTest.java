package com.gymconnect.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void generateUsername_shouldReturnBaseUsername_whenNoConflicts() {
        String result = usernameGenerator.generateUsername("John", "Doe", new ArrayList<>());

        assertEquals("John.Doe", result);
    }

    @Test
    void generateUsername_shouldAppendCounter_whenConflictExists() {
        List<String> existing = List.of("John.Doe");

        String result = usernameGenerator.generateUsername("John", "Doe",
                new ArrayList<>(existing));

        assertEquals("John.Doe1", result);
    }

    @Test
    void generateUsername_shouldIncrementCounter_whenMultipleConflicts() {
        List<String> existing = List.of("John.Doe", "John.Doe1", "John.Doe2");

        String result = usernameGenerator.generateUsername("John", "Doe",
                new ArrayList<>(existing));

        assertEquals("John.Doe3", result);
    }

    @Test
    void generateUsername_shouldHandleDifferentNames() {
        List<String> existing = List.of("John.Doe");

        String result = usernameGenerator.generateUsername("Jane", "Smith",
                new ArrayList<>(existing));

        assertEquals("Jane.Smith", result);
    }
}
