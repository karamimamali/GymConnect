package com.gymconnect.messaging;

import com.gymconnect.dto.WorkloadActionType;
import com.gymconnect.dto.WorkloadRequest;
import com.gymconnect.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes training ADD/DELETE events to the workload service via ActiveMQ.
 *
 * <p>This replaces the former synchronous REST call: the event is dropped onto a
 * queue and the main transaction returns immediately, so a slow or offline
 * workload service can no longer delay or fail the primary use case. If even the
 * broker is unreachable the failure is logged and swallowed — reporting stays a
 * best-effort side concern.</p>
 */
@Component
public class WorkloadEventPublisher {

    /** JMS message property propagating the caller's transaction id (JMS property
     *  names must be Java identifiers, so no {@code X-Transaction-Id} here). */
    public static final String TRANSACTION_ID_PROPERTY = "transactionId";

    private static final Logger logger = LoggerFactory.getLogger(WorkloadEventPublisher.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final JmsTemplate jmsTemplate;
    private final String workloadQueue;

    public WorkloadEventPublisher(JmsTemplate jmsTemplate,
                                  @Value("${app.jms.workload-queue}") String workloadQueue) {
        this.jmsTemplate = jmsTemplate;
        this.workloadQueue = workloadQueue;
    }

    /** Notifies the reporting service that a training was added or removed. */
    public void publish(WorkloadActionType actionType, Training training) {
        WorkloadRequest request = toRequest(actionType, training);
        logger.info("Publishing {} workload event to '{}': {} min for trainer '{}' on {}",
                actionType, workloadQueue, request.trainingDuration(), request.username(),
                request.trainingDate());
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        try {
            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                if (transactionId != null && !transactionId.isBlank()) {
                    message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
                }
                return message;
            });
            logger.debug("Workload event for trainer '{}' enqueued", request.username());
        } catch (JmsException ex) {
            logger.error("Broker unavailable — {} event for trainer '{}' on {} was not "
                            + "enqueued (training still recorded locally): {}",
                    request.actionType(), request.username(), request.trainingDate(),
                    ex.toString());
        }
    }

    private WorkloadRequest toRequest(WorkloadActionType actionType, Training training) {
        var trainerUser = training.getTrainer().getUser();
        return new WorkloadRequest(
                trainerUser.getUsername(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                Boolean.TRUE.equals(trainerUser.getIsActive()),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType);
    }
}
