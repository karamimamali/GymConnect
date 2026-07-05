package com.gymconnect.workload.messaging;

import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.service.TrainerWorkloadService;
import jakarta.jms.TextMessage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadListenerTest {

    private static final String DLQ = "trainer.workload.queue.dlq";

    @Mock
    private TrainerWorkloadService workloadService;
    @Mock
    private JmsTemplate jmsTemplate;

    private TrainerWorkloadListener listener;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        listener = new TrainerWorkloadListener(workloadService, validator, jmsTemplate, DLQ);
    }

    private TrainerWorkloadRequest validRequest() {
        return new TrainerWorkloadRequest("Mike.Johnson", "Mike", "Johnson", true,
                LocalDate.of(2026, 5, 10), 90, ActionType.ADD);
    }

    @Test
    void onWorkloadMessage_shouldProcessValidMessage() {
        TrainerWorkloadRequest request = validRequest();

        listener.onWorkloadMessage(request, "tx-123");

        verify(workloadService).process(request);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void onWorkloadMessage_shouldProcess_whenTransactionIdMissing() {
        TrainerWorkloadRequest request = validRequest();

        listener.onWorkloadMessage(request, null);

        verify(workloadService).process(request);
    }

    @Test
    void onWorkloadMessage_shouldMoveMessageToDlq_whenRequiredInformationMissing() {
        TrainerWorkloadRequest invalid = new TrainerWorkloadRequest(null, "Mike", "Johnson",
                true, LocalDate.of(2026, 5, 10), 90, ActionType.ADD);

        listener.onWorkloadMessage(invalid, "tx-123");

        verify(workloadService, never()).process(any());
        verify(jmsTemplate).convertAndSend(eq(DLQ), eq(invalid),
                any(MessagePostProcessor.class));
    }

    @Test
    void onWorkloadMessage_shouldMoveMessageToDlq_whenDurationNotPositive() {
        TrainerWorkloadRequest invalid = new TrainerWorkloadRequest("Mike.Johnson", "Mike",
                "Johnson", true, LocalDate.of(2026, 5, 10), -5, ActionType.ADD);

        listener.onWorkloadMessage(invalid, null);

        verify(workloadService, never()).process(any());
        verify(jmsTemplate).convertAndSend(eq(DLQ), eq(invalid),
                any(MessagePostProcessor.class));
    }

    @Test
    void deadLetter_shouldCarryTransactionIdAndRejectionReason() throws Exception {
        TrainerWorkloadRequest invalid = new TrainerWorkloadRequest(null, "Mike", "Johnson",
                true, LocalDate.of(2026, 5, 10), 90, ActionType.ADD);

        listener.onWorkloadMessage(invalid, "tx-123");

        ArgumentCaptor<MessagePostProcessor> captor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(DLQ), eq(invalid), captor.capture());
        TextMessage message = mock(TextMessage.class);
        captor.getValue().postProcessMessage(message);
        verify(message).setStringProperty(
                TrainerWorkloadListener.TRANSACTION_ID_PROPERTY, "tx-123");
        verify(message).setStringProperty(eq("rejectionReason"), anyString());
    }

    @Test
    void onWorkloadMessage_shouldRethrow_whenProcessingFails() {
        // Unexpected failures must roll the transacted session back so the broker
        // redelivers the message (and eventually dead-letters it).
        TrainerWorkloadRequest request = validRequest();
        doThrow(new IllegalStateException("boom")).when(workloadService).process(request);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> listener.onWorkloadMessage(request, "tx-123"));
        assertEquals("boom", ex.getMessage());
    }
}
