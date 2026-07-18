package com.gymconnect.workload.cucumber;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Test-side consumer for the application dead letter queue. Uses its own
 * {@link JmsTemplate} (with a bounded receive timeout) so the application's
 * template is left untouched.
 */
@Component
public class DeadLetterQueueProbe {

    private static final long RECEIVE_TIMEOUT_MS = 5000;
    private static final long DRAIN_TIMEOUT_MS = 100;

    private final JmsTemplate probeTemplate;
    private final String deadLetterQueue;

    public DeadLetterQueueProbe(ConnectionFactory connectionFactory,
                                @Value("${app.jms.workload-dlq}") String deadLetterQueue) {
        this.probeTemplate = new JmsTemplate(connectionFactory);
        this.probeTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
        this.deadLetterQueue = deadLetterQueue;
    }

    /** @return the next dead letter, or {@code null} on timeout. */
    public Message receive() {
        return probeTemplate.receive(deadLetterQueue);
    }

    /** Removes any dead letters left over from previous scenarios. */
    public void drain() {
        probeTemplate.setReceiveTimeout(DRAIN_TIMEOUT_MS);
        try {
            while (probeTemplate.receive(deadLetterQueue) != null) {
                // discard
            }
        } finally {
            probeTemplate.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
        }
    }
}
