package com.gymconnect.workload.dto;

import java.util.List;

/**
 * Aggregated monthly workload summary for one trainer.
 *
 * @param username  trainer username
 * @param firstName trainer first name
 * @param lastName  trainer last name
 * @param active    trainer status (active/inactive)
 * @param years     per-year breakdown, ordered by year
 */
public record TrainerWorkloadSummaryResponse(
        String username,
        String firstName,
        String lastName,
        boolean active,
        List<YearSummaryResponse> years
) {
}
