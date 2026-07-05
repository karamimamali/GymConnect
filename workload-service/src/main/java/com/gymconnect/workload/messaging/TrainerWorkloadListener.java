package com.gymconnect.workload.messaging;

import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Consumes trainer workload ADD/DELETE events published by the main service.
 *
 * <p>Each message is validated before processing. Payloads missing required
 * information are not worth redelivering — the data will never appear — so they
 * are moved to the dead letter queue for inspection and acknowledged. Unexpected
 * processing failures, by contrast, are rethrown so the transacted session rolls
 * back and the broker redelivers (and eventually dead-letters) the message.</p>
 */
@Component
public class TrainerWorkloadListener {

    /** JMS property carrying the producer's transaction id for cross-service tracing. */
    public static final String TRANSACTION_ID_PROPERTY = "transactionId";

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadListener.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final TrainerWorkloadService workloadService;
    private final Validator validator;
    private final JmsTemplate jmsTemplate;
    private final String deadLetterQueue;

    public TrainerWorkloadListener(TrainerWorkloadService workloadService,
                                   Validator validator,
                                   JmsTemplate jmsTemplate,
                                   @Value("${app.jms.workload-dlq}") String deadLetterQueue) {
        this.workloadService = workloadService;
        this.validator = validator;
        this.jmsTemplate = jmsTemplate;
        this.deadLetterQueue = deadLetterQueue;
    }

    @JmsListener(destination = "${app.jms.workload-queue}",
            containerFactory = "workloadListenerContainerFactory")
    public void onWorkloadMessage(TrainerWorkloadRequest request,
                                  @Header(name = TRANSACTION_ID_PROPERTY, required = false)
                                  String transactionId) {
        MDC.put(TRANSACTION_ID_KEY, transactionId != null && !transactionId.isBlank()
                ? transactionId : UUID.randomUUID().toString());
        try {
            logger.info("Received {} workload message for trainer '{}'",
                    request.actionType(), request.username());

            Set<ConstraintViolation<TrainerWorkloadRequest>> violations =
                    validator.validate(request);
            if (!violations.isEmpty()) {
                sendToDeadLetterQueue(request, violations);
                return;
            }

            workloadService.process(request);
            logger.debug("Workload message for trainer '{}' processed", request.username());
        } finally {
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    /**
     * Routes a structurally valid but incomplete message to the DLQ. The original
     * transaction id is preserved on the dead letter so it can be traced back to
     * the producing request.
     */
    private void sendToDeadLetterQueue(TrainerWorkloadRequest request,
                                       Set<ConstraintViolation<TrainerWorkloadRequest>> violations) {
        String reasons = violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("; "));
        logger.warn("Invalid workload message for trainer '{}' moved to DLQ '{}': {}",
                request.username(), deadLetterQueue, reasons);
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        jmsTemplate.convertAndSend(deadLetterQueue, request, message -> {
            message.setStringProperty(TRANSACTION_ID_PROPERTY, transactionId);
            message.setStringProperty("rejectionReason", reasons);
            return message;
        });
    }
}
