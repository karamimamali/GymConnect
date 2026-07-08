package com.gymconnect.workload.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadTest {

    private TrainerWorkload newWorkload() {
        return new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
    }

    private long duration(TrainerWorkload workload, int year, int month) {
        return workload.findYear(year).flatMap(y -> y.findMonth(month))
                .map(MonthSummary::getTrainingSummaryDuration)
                .orElseThrow();
    }

    @Test
    void addDuration_shouldAccumulateWithinSameMonth() {
        TrainerWorkload workload = newWorkload();

        workload.addDuration(2026, 5, 60);
        workload.addDuration(2026, 5, 30);

        assertEquals(90, duration(workload, 2026, 5));
    }

    @Test
    void addDuration_shouldKeepYearsAndMonthsSeparate() {
        TrainerWorkload workload = newWorkload();

        workload.addDuration(2025, 12, 45);
        workload.addDuration(2026, 1, 15);

        assertEquals(45, duration(workload, 2025, 12));
        assertEquals(15, duration(workload, 2026, 1));
    }

    @Test
    void addDuration_shouldKeepYearsAndMonthsOrdered() {
        TrainerWorkload workload = newWorkload();

        workload.addDuration(2026, 1, 10);
        workload.addDuration(2025, 12, 20);
        workload.addDuration(2025, 3, 30);

        List<YearSummary> years = workload.getYears();
        assertEquals(2025, years.get(0).getYear());
        assertEquals(2026, years.get(1).getYear());
        // Months within 2025 are ordered ascending: March before December.
        assertEquals(3, years.get(0).getMonths().get(0).getMonth());
        assertEquals(12, years.get(0).getMonths().get(1).getMonth());
    }

    @Test
    void subtractDuration_shouldReduceTotal() {
        TrainerWorkload workload = newWorkload();
        workload.addDuration(2026, 5, 90);

        workload.subtractDuration(2026, 5, 30);

        assertEquals(60, duration(workload, 2026, 5));
    }

    @Test
    void subtractDuration_shouldPruneMonthAndYear_whenReachingZero() {
        TrainerWorkload workload = newWorkload();
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2026, 5, 60);

        assertTrue(workload.getYears().isEmpty());
    }

    @Test
    void subtractDuration_shouldFloorAtZeroAndPrune_whenOverSubtracting() {
        TrainerWorkload workload = newWorkload();
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2026, 5, 100);

        assertTrue(workload.findYear(2026).isEmpty());
    }

    @Test
    void subtractDuration_shouldKeepOtherMonths_whenOnePruned() {
        TrainerWorkload workload = newWorkload();
        workload.addDuration(2026, 5, 60);
        workload.addDuration(2026, 6, 30);

        workload.subtractDuration(2026, 5, 60);

        assertTrue(workload.findYear(2026).flatMap(y -> y.findMonth(5)).isEmpty());
        assertEquals(30, duration(workload, 2026, 6));
    }

    @Test
    void subtractDuration_shouldBeNoOp_whenYearOrMonthUnknown() {
        TrainerWorkload workload = newWorkload();
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2030, 1, 10); // unknown year
        workload.subtractDuration(2026, 7, 10); // unknown month

        assertEquals(60, duration(workload, 2026, 5));
    }

    @Test
    void updateIdentity_shouldRefreshMutableFields() {
        TrainerWorkload workload = newWorkload();

        workload.updateIdentity("Michael", "Johnston", false);

        assertEquals("Michael", workload.getFirstName());
        assertEquals("Johnston", workload.getLastName());
        assertFalse(workload.getActive());
        assertEquals("Mike.Johnson", workload.getUsername());
    }
}
