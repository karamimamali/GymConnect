package com.gymconnect.messaging;

import com.gymconnect.dto.WorkloadActionType;
import com.gymconnect.dto.WorkloadRequest;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import com.gymconnect.model.TrainingType;
import com.gymconnect.model.User;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadEventPublisherTest {

    private static final String QUEUE = "trainer.workload.queue";

    @Mock
    private JmsTemplate jmsTemplate;

    private WorkloadEventPublisher publisher;
    private Training training;

    @BeforeEach
    void setUp() {
        publisher = new WorkloadEventPublisher(jmsTemplate, QUEUE);

        User trainerUser = new User("Mike", "Johnson", true);
        trainerUser.setUsername("Mike.Johnson");
        Trainer trainer = new Trainer(trainerUser, new TrainingType("FITNESS"));
        training = new Training(null, trainer, "Cardio",
                new TrainingType("FITNESS"), LocalDate.of(2026, 5, 10), 90);
    }

    @AfterEach
    void tearDown() {
        MDC.remove("transactionId");
    }

    @Test
    void publish_shouldSendMappedRequestToWorkloadQueue() {
        publisher.publish(WorkloadActionType.ADD, training);

        ArgumentCaptor<WorkloadRequest> captor = ArgumentCaptor.forClass(WorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq(QUEUE), captor.capture(),
                any(MessagePostProcessor.class));
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
    void publish_shouldPropagateTransactionIdAsMessageProperty() throws Exception {
        MDC.put("transactionId", "tx-123");

        publisher.publish(WorkloadActionType.ADD, training);

        ArgumentCaptor<MessagePostProcessor> captor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(QUEUE), any(WorkloadRequest.class),
                captor.capture());
        TextMessage message = mock(TextMessage.class);
        captor.getValue().postProcessMessage(message);
        verify(message).setStringProperty(
                WorkloadEventPublisher.TRANSACTION_ID_PROPERTY, "tx-123");
    }

    @Test
    void publish_shouldNotSetTransactionIdProperty_whenNoneInContext() throws Exception {
        publisher.publish(WorkloadActionType.DELETE, training);

        ArgumentCaptor<MessagePostProcessor> captor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(QUEUE), any(WorkloadRequest.class),
                captor.capture());
        TextMessage message = mock(TextMessage.class);
        captor.getValue().postProcessMessage(message);
        verify(message, never()).setStringProperty(
                eq(WorkloadEventPublisher.TRANSACTION_ID_PROPERTY), any());
    }

    @Test
    void publish_shouldNotThrow_whenBrokerUnavailable() {
        // Reporting is best-effort: a broker outage must not break the primary use case.
        doThrow(new UncategorizedJmsException("broker down"))
                .when(jmsTemplate)
                .convertAndSend(eq(QUEUE), any(WorkloadRequest.class),
                        any(MessagePostProcessor.class));

        assertDoesNotThrow(() -> publisher.publish(WorkloadActionType.DELETE, training));
    }
}
