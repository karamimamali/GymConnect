package com.gymconnect.workload.dto;

/**
 * Total training duration accrued by a trainer in a single month.
 *
 * @param month                   month number, 1 (January) – 12 (December)
 * @param trainingSummaryDuration summed training duration in minutes for that month
 */
public record MonthSummaryResponse(
        int month,
        long trainingSummaryDuration
) {
}
