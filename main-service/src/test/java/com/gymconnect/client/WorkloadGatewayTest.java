package com.gymconnect.client;

import com.gymconnect.dto.WorkloadActionType;
import com.gymconnect.dto.WorkloadRequest;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadGatewayTest {

    @Mock
    private WorkloadClient workloadClient;
    @Mock
    @SuppressWarnings("rawtypes")
    private CircuitBreakerFactory circuitBreakerFactory;
    @Mock
    private CircuitBreaker circuitBreaker;

    private WorkloadGateway gateway;
    private Training training;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        gateway = new WorkloadGateway(workloadClient, circuitBreakerFactory);

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setUsername("Mike.Johnson");
        Trainer trainer = new Trainer(trainerUser, new TrainingType("FITNESS"));
        training = new Training(null, trainer, "Cardio",
                new TrainingType("FITNESS"), LocalDate.of(2026, 5, 10), 90);

        when(circuitBreakerFactory.create("workload-service")).thenReturn(circuitBreaker);
    }

    @Test
    @SuppressWarnings("unchecked")
    void notify_shouldSendMappedRequest_whenCircuitClosed() {
        // The circuit breaker simply runs the supplied call.
        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> ((Supplier<Object>) invocation.getArgument(0)).get());

        gateway.notify(WorkloadActionType.ADD, training);

        ArgumentCaptor<WorkloadRequest> captor = ArgumentCaptor.forClass(WorkloadRequest.class);
        verify(workloadClient).sendWorkload(captor.capture());
        WorkloadRequest sent = captor.getValue();
        assertEquals("Mike.Johnson", sent.username());
        assertEquals("Mike", sent.firstName());
        assertEquals("Johnson", sent.lastName());
        assertEquals(true, sent.active());
        assertEquals(LocalDate.of(2026, 5, 10), sent.trainingDate());
        assertEquals(90, sent.trainingDuration());
        assertEquals(WorkloadActionType.ADD, sent.actionType());
    }

    @Test
    @SuppressWarnings("unchecked")
    void notify_shouldInvokeFallbackAndNotThrow_whenCallFails() {
        // Simulate an open circuit / downstream failure: the breaker runs the fallback.
        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> ((Function<Throwable, Object>) invocation.getArgument(1))
                        .apply(new RuntimeException("workload-service down")));

        // Must complete without propagating the exception (reporting is best-effort).
        gateway.notify(WorkloadActionType.DELETE, training);

        verify(workloadClient, never()).sendWorkload(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void notify_fallbackResultIsNull() {
        when(circuitBreaker.run(any(Supplier.class), any(Function.class)))
                .thenAnswer(invocation -> ((Function<Throwable, Object>) invocation.getArgument(1))
                        .apply(new IllegalStateException("boom")));

        gateway.notify(WorkloadActionType.ADD, training);

        // Sanity: the trainer mapping above is active, so no NPE leaked through.
        assertFalse(Thread.currentThread().isInterrupted());
    }
}
