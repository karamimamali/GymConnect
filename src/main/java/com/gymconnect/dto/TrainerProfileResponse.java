package com.gymconnect.dto;

import java.util.List;

public record TrainerProfileResponse(
        String firstName,
        String lastName,
        String specialization,
        Boolean isActive,
        List<TraineeSummary> trainees
) {
}
