package com.gymconnect.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generatePassword_shouldReturnNonNull() {
        String password = passwordGenerator.generatePassword();

        assertNotNull(password);
    }

    @Test
    void generatePassword_shouldReturnTenCharacters() {
        String password = passwordGenerator.generatePassword();

        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_shouldContainOnlyAlphanumeric() {
        String password = passwordGenerator.generatePassword();

        assertTrue(password.matches("[A-Za-z0-9]+"));
    }

    @Test
    void generatePassword_shouldGenerateUniquePasswords() {
        String password1 = passwordGenerator.generatePassword();
        String password2 = passwordGenerator.generatePassword();

        assertNotEquals(password1, password2);
    }
}
