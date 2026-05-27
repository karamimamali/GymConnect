package com.gymconnect.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleSecurityReturns401() {
        ResponseEntity<Map<String, String>> response =
                handler.handleSecurity(new SecurityException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgumentNotFoundReturns404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Trainee not found: x"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleIllegalArgumentBadRequestReturns400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("First name is required"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleIllegalStateReturns400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalState(new IllegalStateException("Already active"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Already active", response.getBody().get("error"));
    }

    @Test
    void handleValidationReturns400() throws Exception {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "obj");
        result.addError(new FieldError("obj", "firstName", "is required"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, result);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().get("error"));
    }

    @Test
    void handleMissingParamReturns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("password", "String");
        ResponseEntity<Map<String, String>> response = handler.handleMissingParam(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleGenericReturns500() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGeneric(new RuntimeException("Unexpected"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
