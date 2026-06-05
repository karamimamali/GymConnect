package com.gymconnect.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {

    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        gymMetrics = new GymMetrics(new SimpleMeterRegistry());
    }

    @Test
    void recordTraineeRegistration_shouldIncrementCounterAndGauge() {
        gymMetrics.recordTraineeRegistration();
        gymMetrics.recordTraineeRegistration();

        assertEquals(2.0, gymMetrics.getTraineeRegistrationsCount());
        assertEquals(2, gymMetrics.getActiveTraineeCount());
    }

    @Test
    void recordTraineeDeletion_shouldDecrementGauge() {
        gymMetrics.recordTraineeRegistration();
        gymMetrics.recordTraineeRegistration();
        gymMetrics.recordTraineeDeletion();

        assertEquals(2.0, gymMetrics.getTraineeRegistrationsCount());
        assertEquals(1, gymMetrics.getActiveTraineeCount());
    }

    @Test
    void recordTrainerRegistration_shouldIncrementCounterAndGauge() {
        gymMetrics.recordTrainerRegistration();

        assertEquals(1.0, gymMetrics.getTrainerRegistrationsCount());
        assertEquals(1, gymMetrics.getActiveTrainerCount());
    }

    @Test
    void recordAuthenticationSuccess_shouldIncrementCounter() {
        gymMetrics.recordAuthenticationSuccess();
        gymMetrics.recordAuthenticationSuccess();

        assertEquals(2.0, gymMetrics.getAuthenticationSuccessesCount());
    }

    @Test
    void recordAuthenticationFailure_shouldIncrementCounter() {
        gymMetrics.recordAuthenticationFailure();

        assertEquals(1.0, gymMetrics.getAuthenticationFailuresCount());
    }

    @Test
    void initialState_allCountersAndGaugesAreZero() {
        assertEquals(0.0, gymMetrics.getTraineeRegistrationsCount());
        assertEquals(0.0, gymMetrics.getTrainerRegistrationsCount());
        assertEquals(0.0, gymMetrics.getAuthenticationSuccessesCount());
        assertEquals(0.0, gymMetrics.getAuthenticationFailuresCount());
        assertEquals(0, gymMetrics.getActiveTraineeCount());
        assertEquals(0, gymMetrics.getActiveTrainerCount());
    }

    @Test
    void recordAuthenticationSuccess_andFailure_areIndependent() {
        gymMetrics.recordAuthenticationSuccess();
        gymMetrics.recordAuthenticationFailure();
        gymMetrics.recordAuthenticationFailure();

        assertEquals(1.0, gymMetrics.getAuthenticationSuccessesCount());
        assertEquals(2.0, gymMetrics.getAuthenticationFailuresCount());
    }
}
