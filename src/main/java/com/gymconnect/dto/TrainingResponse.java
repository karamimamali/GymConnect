package com.gymconnect.dto;

import java.time.LocalDate;

public record TrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String trainerName
) {
}
