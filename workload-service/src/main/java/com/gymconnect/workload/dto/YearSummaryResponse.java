package com.gymconnect.workload.dto;

import java.util.List;

/**
 * A trainer's monthly training totals grouped under a single year.
 *
 * @param year   calendar year
 * @param months per-month totals, ordered by month
 */
public record YearSummaryResponse(
        int year,
        List<MonthSummaryResponse> months
) {
}
