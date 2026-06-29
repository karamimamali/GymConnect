package com.gymconnect.workload.service;

import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;
import com.gymconnect.workload.exception.TrainerWorkloadNotFoundException;
import com.gymconnect.workload.repository.InMemoryTrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadServiceImplTest {

    private TrainerWorkloadServiceImpl service;

    @BeforeEach
    void setUp() {
        // Use the real in-memory repository for a behaviour-focused test.
        service = new TrainerWorkloadServiceImpl(new InMemoryTrainerWorkloadRepository());
    }

    private TrainerWorkloadRequest request(ActionType action, LocalDate date, int duration) {
        return new TrainerWorkloadRequest("Mike.Johnson", "Mike", "Johnson", true,
                date, duration, action);
    }

    @Test
    void process_add_shouldCreateSummaryWithAccruedDuration() {
        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 10), 60));
        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 20), 30));

        TrainerWorkloadSummaryResponse summary = service.getSummary("Mike.Johnson");

        assertEquals("Mike.Johnson", summary.username());
        assertTrue(summary.active());
        assertEquals(2026, summary.years().get(0).year());
        assertEquals(5, summary.years().get(0).months().get(0).month());
        assertEquals(90, summary.years().get(0).months().get(0).trainingSummaryDuration());
    }

    @Test
    void process_add_shouldGroupByYearAndMonthInOrder() {
        service.process(request(ActionType.ADD, LocalDate.of(2026, 1, 5), 20));
        service.process(request(ActionType.ADD, LocalDate.of(2025, 12, 5), 40));

        TrainerWorkloadSummaryResponse summary = service.getSummary("Mike.Johnson");

        assertEquals(2025, summary.years().get(0).year());
        assertEquals(2026, summary.years().get(1).year());
    }

    @Test
    void process_delete_shouldReduceAccruedDuration() {
        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 10), 90));

        service.process(request(ActionType.DELETE, LocalDate.of(2026, 5, 10), 30));

        TrainerWorkloadSummaryResponse summary = service.getSummary("Mike.Johnson");
        assertEquals(60, summary.years().get(0).months().get(0).trainingSummaryDuration());
    }

    @Test
    void process_delete_shouldLeaveEmptySummary_whenAllReversed() {
        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 10), 60));

        service.process(request(ActionType.DELETE, LocalDate.of(2026, 5, 10), 60));

        TrainerWorkloadSummaryResponse summary = service.getSummary("Mike.Johnson");
        assertTrue(summary.years().isEmpty());
    }

    @Test
    void process_delete_shouldBeNoOp_whenTrainerUnknown() {
        service.process(request(ActionType.DELETE, LocalDate.of(2026, 5, 10), 60));

        // No record was created by a DELETE for an unknown trainer.
        assertThrows(TrainerWorkloadNotFoundException.class,
                () -> service.getSummary("Mike.Johnson"));
    }

    @Test
    void process_shouldRefreshTrainerStatus_onLaterEvents() {
        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 10), 60));

        TrainerWorkloadRequest inactive = new TrainerWorkloadRequest("Mike.Johnson", "Mike",
                "Johnson", false, LocalDate.of(2026, 6, 1), 30, ActionType.ADD);
        service.process(inactive);

        assertFalse(service.getSummary("Mike.Johnson").active());
    }

    @Test
    void getSummary_shouldThrow_whenTrainerHasNoData() {
        assertThrows(TrainerWorkloadNotFoundException.class,
                () -> service.getSummary("Nobody"));
    }
}
