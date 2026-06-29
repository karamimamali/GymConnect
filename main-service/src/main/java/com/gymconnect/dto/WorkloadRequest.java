package com.gymconnect.dto;

import java.time.LocalDate;

/**
 * Payload sent to the workload (reporting) microservice describing a single
 * training that was added or removed for a trainer.
 */
public record WorkloadRequest(
        String username,
        String firstName,
        String lastName,
        boolean active,
        LocalDate trainingDate,
        int trainingDuration,
        WorkloadActionType actionType
) {
}
