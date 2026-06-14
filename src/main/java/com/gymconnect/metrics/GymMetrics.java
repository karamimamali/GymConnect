package com.gymconnect.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central component that registers and exposes custom Prometheus metrics
 * for the GymConnect application.
 *
 * <p>Metrics exposed:
 * <ul>
 *   <li>{@code gym_trainee_registrations_total} – cumulative trainee registrations</li>
 *   <li>{@code gym_trainer_registrations_total} – cumulative trainer registrations</li>
 *   <li>{@code gym_authentication_successes_total} – cumulative successful logins</li>
 *   <li>{@code gym_authentication_failures_total} – cumulative failed login attempts</li>
 *   <li>{@code gym_active_trainees} – current gauge of registered (active) trainees</li>
 *   <li>{@code gym_active_trainers} – current gauge of registered (active) trainers</li>
 * </ul>
 */
@Component
public class GymMetrics {

    private static final Logger logger = LoggerFactory.getLogger(GymMetrics.class);

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter authenticationSuccesses;
    private final Counter authenticationFailures;
    private final AtomicInteger activeTraineeCount;
    private final AtomicInteger activeTrainerCount;

    public GymMetrics(MeterRegistry meterRegistry) {
        traineeRegistrations = Counter.builder("gym.trainee.registrations")
                .description("Total number of trainee registrations since startup")
                .register(meterRegistry);

        trainerRegistrations = Counter.builder("gym.trainer.registrations")
                .description("Total number of trainer registrations since startup")
                .register(meterRegistry);

        authenticationSuccesses = Counter.builder("gym.authentication.successes")
                .description("Total number of successful authentication attempts")
                .register(meterRegistry);

        authenticationFailures = Counter.builder("gym.authentication.failures")
                .description("Total number of failed authentication attempts")
                .register(meterRegistry);

        activeTraineeCount = new AtomicInteger(0);
        Gauge.builder("gym.active.trainees", activeTraineeCount, AtomicInteger::get)
                .description("Current number of registered trainees")
                .register(meterRegistry);

        activeTrainerCount = new AtomicInteger(0);
        Gauge.builder("gym.active.trainers", activeTrainerCount, AtomicInteger::get)
                .description("Current number of registered trainers")
                .register(meterRegistry);
    }

    /** Called when a new trainee is successfully registered. */
    public void recordTraineeRegistration() {
        logger.debug("Recording trainee registration metric");
        traineeRegistrations.increment();
        activeTraineeCount.incrementAndGet();
    }

    /** Called when a trainee profile is deleted. */
    public void recordTraineeDeletion() {
        logger.debug("Recording trainee deletion metric");
        activeTraineeCount.decrementAndGet();
    }

    /** Called when a new trainer is successfully registered. */
    public void recordTrainerRegistration() {
        logger.debug("Recording trainer registration metric");
        trainerRegistrations.increment();
        activeTrainerCount.incrementAndGet();
    }

    /** Called after a successful authentication check (trainee or trainer). */
    public void recordAuthenticationSuccess() {
        logger.debug("Recording authentication success metric");
        authenticationSuccesses.increment();
    }

    /** Called after a failed authentication check (trainee or trainer). */
    public void recordAuthenticationFailure() {
        logger.debug("Recording authentication failure metric");
        authenticationFailures.increment();
    }

    // --- Accessors for testing ---

    double getTraineeRegistrationsCount() {
        return traineeRegistrations.count();
    }

    double getTrainerRegistrationsCount() {
        return trainerRegistrations.count();
    }

    double getAuthenticationSuccessesCount() {
        return authenticationSuccesses.count();
    }

    double getAuthenticationFailuresCount() {
        return authenticationFailures.count();
    }

    int getActiveTraineeCount() {
        return activeTraineeCount.get();
    }

    int getActiveTrainerCount() {
        return activeTrainerCount.get();
    }
}
