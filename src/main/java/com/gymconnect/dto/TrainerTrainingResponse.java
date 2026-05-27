package com.gymconnect.dto;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String traineeName
) {
}
