package com.gymconnect.workload.exception;

/**
 * Raised when a workload summary is requested for a trainer that has no recorded
 * training data. Mapped to HTTP 404 by the global exception handler.
 */
public class TrainerWorkloadNotFoundException extends RuntimeException {

    public TrainerWorkloadNotFoundException(String username) {
        super("No workload recorded for trainer: " + username);
    }
}
