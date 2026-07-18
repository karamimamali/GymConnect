package com.gymconnect.cucumber;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Test-side consumer for the trainer workload queue. Uses its own
 * {@link JmsTemplate} (with a bounded receive timeout) so the application's
 * template is left untouched.
 */
@Component
public class WorkloadQueueProbe {

    private static final long RECEIVE_TIMEOUT_MS = 3000;
    private static final long DRAIN_TIMEOUT_MS = 100;

    private final JmsTemplate probeTemplate;
    private final String workloadQueue;

    public WorkloadQueueProbe(ConnectionFactory connectionFactory,
                              @Value("${app.jms.workload-queue}") String workloadQueue) {
        this.probeTemplate = new JmsTemplate(connectionFactory);
        this.probeTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
        this.workloadQueue = workloadQueue;
    }

    /** @return the next message on the workload queue, or {@code null} on timeout. */
    public Message receive() {
        return probeTemplate.receive(workloadQueue);
    }

    /** Removes any messages left over from previous scenarios. */
    public void drain() {
        probeTemplate.setReceiveTimeout(DRAIN_TIMEOUT_MS);
        try {
            while (probeTemplate.receive(workloadQueue) != null) {
                // discard
            }
        } finally {
            probeTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
        }
    }
}
