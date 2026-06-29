package com.gymconnect.client;

import com.gymconnect.dto.WorkloadActionType;
import com.gymconnect.dto.WorkloadRequest;
import com.gymconnect.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

/**
 * Sends training ADD/DELETE events to the workload microservice, guarded by a
 * circuit breaker.
 *
 * <p>The reporting call is intentionally best-effort: if the workload service is
 * down, slow, or the circuit is open, the {@link #reportFailure fallback} simply
 * logs the problem so the core training operation still succeeds. Reporting is a
 * side concern and must never break the primary use case.</p>
 */
@Component
public class WorkloadGateway {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadGateway.class);
    private static final String CIRCUIT_BREAKER_ID = "workload-service";
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final WorkloadClient workloadClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public WorkloadGateway(WorkloadClient workloadClient,
                           CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.workloadClient = workloadClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    /** Notifies the reporting service that a training was added or removed. */
    public void notify(WorkloadActionType actionType, Training training) {
        WorkloadRequest request = toRequest(actionType, training);
        logger.info("Notifying workload service: {} {} min for trainer '{}' on {}",
                actionType, request.trainingDuration(), request.username(), request.trainingDate());
        // The circuit breaker's time limiter runs the call on a separate thread, which
        // would lose the MDC transaction id. Capture it here and re-establish it inside
        // the worker so it propagates downstream via the X-Transaction-Id header.
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        circuitBreakerFactory.create(CIRCUIT_BREAKER_ID).run(
                () -> {
                    if (transactionId != null) {
                        MDC.put(TRANSACTION_ID_KEY, transactionId);
                    }
                    try {
                        workloadClient.sendWorkload(request);
                        return null;
                    } finally {
                        MDC.remove(TRANSACTION_ID_KEY);
                    }
                },
                throwable -> reportFailure(request, throwable));
    }

    private Void reportFailure(WorkloadRequest request, Throwable throwable) {
        logger.error("Workload service unavailable — {} event for trainer '{}' on {} was not "
                        + "delivered (training still recorded locally): {}",
                request.actionType(), request.username(), request.trainingDate(),
                throwable.toString());
        return null;
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
