package com.gymconnect.dto;

import java.util.List;

public record UpdateTrainerResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        Boolean isActive,
        List<TraineeSummary> trainees
) {
}
