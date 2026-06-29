package com.gymconnect.workload.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadTest {

    @Test
    void addDuration_shouldAccumulateWithinSameMonth() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);

        workload.addDuration(2026, 5, 60);
        workload.addDuration(2026, 5, 30);

        assertEquals(90, workload.snapshot().get(2026).get(5));
    }

    @Test
    void addDuration_shouldKeepYearsAndMonthsSeparate() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);

        workload.addDuration(2025, 12, 45);
        workload.addDuration(2026, 1, 15);

        Map<Integer, Map<Integer, Integer>> snapshot = workload.snapshot();
        assertEquals(45, snapshot.get(2025).get(12));
        assertEquals(15, snapshot.get(2026).get(1));
    }

    @Test
    void subtractDuration_shouldReduceTotal() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 90);

        workload.subtractDuration(2026, 5, 30);

        assertEquals(60, workload.snapshot().get(2026).get(5));
    }

    @Test
    void subtractDuration_shouldPruneMonthAndYear_whenReachingZero() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2026, 5, 60);

        assertTrue(workload.snapshot().isEmpty());
    }

    @Test
    void subtractDuration_shouldFloorAtZeroAndPrune_whenOverSubtracting() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2026, 5, 100);

        assertFalse(workload.snapshot().containsKey(2026));
    }

    @Test
    void subtractDuration_shouldBeNoOp_whenYearOrMonthUnknown() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 60);

        workload.subtractDuration(2030, 1, 10); // unknown year
        workload.subtractDuration(2026, 7, 10); // unknown month

        assertEquals(60, workload.snapshot().get(2026).get(5));
    }

    @Test
    void updateIdentity_shouldRefreshMutableFields() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);

        workload.updateIdentity("Michael", "Johnston", false);

        assertEquals("Michael", workload.getFirstName());
        assertEquals("Johnston", workload.getLastName());
        assertFalse(workload.isActive());
        assertEquals("Mike.Johnson", workload.getUsername());
    }

    @Test
    void snapshot_shouldBeIndependentCopy() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 60);

        Map<Integer, Map<Integer, Integer>> snapshot = workload.snapshot();
        snapshot.get(2026).put(5, 9999);

        assertEquals(60, workload.snapshot().get(2026).get(5));
    }
}
