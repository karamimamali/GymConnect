package com.gymconnect.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotEmpty(message = "Trainers list is required") List<String> trainerUsernames
) {
}
