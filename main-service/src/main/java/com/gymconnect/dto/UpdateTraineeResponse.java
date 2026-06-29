package com.gymconnect.dto;

import java.time.LocalDate;
import java.util.List;

public record UpdateTraineeResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        Boolean isActive,
        List<TrainerSummary> trainers
) {
}
