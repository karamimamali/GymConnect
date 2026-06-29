package com.gymconnect.workload.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new TrainerWorkloadNotFoundException("Mike.Johnson"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Mike.Johnson"));
    }

    @Test
    void handleValidation_shouldReturn400_withFieldMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("req", "username", "Trainer username is required")));

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Trainer username is required", response.getBody().get("error"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void handleUnreadable_shouldReturn400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUnreadable(new HttpMessageNotReadableException("broken json"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").toLowerCase().contains("malformed"));
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad input", response.getBody().get("error"));
    }

    @Test
    void handleGeneric_shouldReturn500_withoutLeakingDetails() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGeneric(new NullPointerException("npe at line 42"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        // The raw exception detail must not be exposed to the client.
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
