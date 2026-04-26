package com.gymconnect.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
    }

    @Test
    void generateUsername_shouldReturnBaseUsernameWhenNoDuplicates() {
        String username = usernameGenerator.generateUsername("John", "Smith", Collections.emptyList());

        assertEquals("John.Smith", username);
    }

    @Test
    void generateUsername_shouldAppendSerialNumberWhenDuplicateExists() {
        List<String> existing = new ArrayList<>(List.of("John.Smith"));

        String username = usernameGenerator.generateUsername("John", "Smith", existing);

        assertEquals("John.Smith1", username);
    }

    @Test
    void generateUsername_shouldIncrementSerialNumberForMultipleDuplicates() {
        List<String> existing = new ArrayList<>(Arrays.asList("John.Smith", "John.Smith1", "John.Smith2"));

        String username = usernameGenerator.generateUsername("John", "Smith", existing);

        assertEquals("John.Smith3", username);
    }

    @Test
    void generateUsername_shouldHandleDifferentNames() {
        List<String> existing = new ArrayList<>(List.of("John.Smith"));

        String username = usernameGenerator.generateUsername("Jane", "Doe", existing);

        assertEquals("Jane.Doe", username);
    }

    @Test
    void generateUsername_shouldConcatenateWithDotSeparator() {
        String username = usernameGenerator.generateUsername("Alice", "Brown", Collections.emptyList());

        assertTrue(username.contains("."));
        assertEquals("Alice.Brown", username);
    }
}
