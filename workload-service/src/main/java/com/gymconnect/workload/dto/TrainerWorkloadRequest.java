package com.gymconnect.workload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Workload event published by the main service whenever a training session is
 * planned or cancelled for a trainer.
 *
 * @param username         trainer username (unique key of the report)
 * @param firstName        trainer first name
 * @param lastName         trainer last name
 * @param active           whether the trainer is currently active
 * @param trainingDate     date the training takes place (drives the year/month bucket)
 * @param trainingDuration training duration in minutes (must be positive)
 * @param actionType       ADD to accrue the duration, DELETE to reverse it
 */
public record TrainerWorkloadRequest(
        @NotBlank(message = "Trainer username is required") String username,
        @NotBlank(message = "Trainer first name is required") String firstName,
        @NotBlank(message = "Trainer last name is required") String lastName,
        @NotNull(message = "Trainer active status is required") Boolean active,
        @NotNull(message = "Training date is required") LocalDate trainingDate,
        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be positive") Integer trainingDuration,
        @NotNull(message = "Action type is required") ActionType actionType
) {
}
