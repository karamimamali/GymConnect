package com.gymconnect.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void generatePassword_shouldReturn10CharacterString() {
        String password = passwordGenerator.generatePassword();

        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_shouldContainOnlyAlphanumericCharacters() {
        String password = passwordGenerator.generatePassword();

        assertTrue(password.matches("[A-Za-z0-9]+"));
    }

    @Test
    void generatePassword_shouldGenerateUniquePasswords() {
        Set<String> passwords = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            passwords.add(passwordGenerator.generatePassword());
        }

        assertEquals(100, passwords.size());
    }

    @Test
    void generatePassword_shouldNotReturnNull() {
        String password = passwordGenerator.generatePassword();

        assertNotNull(password);
    }

    @Test
    void generatePassword_shouldNotReturnEmptyString() {
        String password = passwordGenerator.generatePassword();

        assertFalse(password.isEmpty());
    }
}
