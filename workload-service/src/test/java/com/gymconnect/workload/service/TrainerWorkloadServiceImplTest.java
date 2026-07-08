package com.gymconnect.workload.service;

import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;
import com.gymconnect.workload.exception.TrainerWorkloadNotFoundException;
import com.gymconnect.workload.model.TrainerWorkload;
import com.gymconnect.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    @Mock
    private TrainerWorkloadRepository repository;

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    private TrainerWorkloadRequest request(ActionType action, LocalDate date, int duration) {
        return new TrainerWorkloadRequest("Mike.Johnson", "Mike", "Johnson", true,
                date, duration, action);
    }

    private TrainerWorkload captureSaved() {
        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void process_add_shouldCreateNewDocument_whenTrainerUnknown() {
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.empty());

        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 10), 60));

        TrainerWorkload saved = captureSaved();
        assertEquals("Mike.Johnson", saved.getUsername());
        assertEquals("Mike", saved.getFirstName());
        assertEquals("Johnson", saved.getLastName());
        assertTrue(saved.getActive());
        assertEquals(60, saved.findYear(2026).flatMap(y -> y.findMonth(5)).orElseThrow()
                .getTrainingSummaryDuration());
    }

    @Test
    void process_add_shouldAccrueOntoExistingRecord() {
        TrainerWorkload existing = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        existing.addDuration(2026, 5, 60);
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.of(existing));

        service.process(request(ActionType.ADD, LocalDate.of(2026, 5, 20), 30));

        TrainerWorkload saved = captureSaved();
        assertEquals(90, saved.findYear(2026).flatMap(y -> y.findMonth(5)).orElseThrow()
                .getTrainingSummaryDuration());
    }

    @Test
    void process_add_shouldRefreshIdentity_onExistingDocument() {
        TrainerWorkload existing = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.of(existing));

        TrainerWorkloadRequest renamed = new TrainerWorkloadRequest("Mike.Johnson", "Michael",
                "Johnston", false, LocalDate.of(2026, 6, 1), 30, ActionType.ADD);
        service.process(renamed);

        TrainerWorkload saved = captureSaved();
        assertEquals("Michael", saved.getFirstName());
        assertEquals("Johnston", saved.getLastName());
        assertFalse(saved.getActive());
    }

    @Test
    void process_delete_shouldReduceAccruedDuration() {
        TrainerWorkload existing = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        existing.addDuration(2026, 5, 90);
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.of(existing));

        service.process(request(ActionType.DELETE, LocalDate.of(2026, 5, 10), 30));

        TrainerWorkload saved = captureSaved();
        assertEquals(60, saved.findYear(2026).flatMap(y -> y.findMonth(5)).orElseThrow()
                .getTrainingSummaryDuration());
    }

    @Test
    void process_delete_shouldBeNoOp_whenTrainerUnknown() {
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.empty());

        service.process(request(ActionType.DELETE, LocalDate.of(2026, 5, 10), 30));

        verify(repository, never()).save(any());
    }

    @Test
    void getSummary_shouldMapDocumentToResponse() {
        TrainerWorkload existing = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        existing.addDuration(2026, 5, 60);
        existing.addDuration(2026, 5, 30);
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.of(existing));

        TrainerWorkloadSummaryResponse summary = service.getSummary("Mike.Johnson");

        assertEquals("Mike.Johnson", summary.username());
        assertTrue(summary.active());
        assertEquals(2026, summary.years().get(0).year());
        assertEquals(5, summary.years().get(0).months().get(0).month());
        assertEquals(90, summary.years().get(0).months().get(0).trainingSummaryDuration());
    }

    @Test
    void getSummary_shouldTreatNullActiveAsInactive() {
        TrainerWorkload existing = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", null);
        when(repository.findByUsername("Mike.Johnson")).thenReturn(Optional.of(existing));

        assertFalse(service.getSummary("Mike.Johnson").active());
    }

    @Test
    void getSummary_shouldThrow_whenTrainerHasNoData() {
        when(repository.findByUsername("Nobody")).thenReturn(Optional.empty());

        assertThrows(TrainerWorkloadNotFoundException.class,
                () -> service.getSummary("Nobody"));
    }
}
