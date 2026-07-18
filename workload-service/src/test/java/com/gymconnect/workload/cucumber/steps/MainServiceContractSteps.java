package com.gymconnect.workload.cucumber.steps;

import com.gymconnect.workload.config.JmsConfig;
import io.cucumber.java.en.When;
import jakarta.jms.TextMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

/**
 * Feeds the workload service the exact wire format the main service produces:
 * a JSON text message whose {@code _type} property identifies the payload.
 * This pins down the inter-service contract without booting the main service.
 */
public class MainServiceContractSteps {

    private final JmsTemplate jmsTemplate;
    private final String workloadQueue;

    public MainServiceContractSteps(JmsTemplate jmsTemplate,
                                    @Value("${app.jms.workload-queue}") String workloadQueue) {
        this.jmsTemplate = jmsTemplate;
        this.workloadQueue = workloadQueue;
    }

    @When("the following message arrives on the workload queue with type id {string}:")
    public void theFollowingMessageArrivesOnTheWorkloadQueue(String typeId, String json) {
        jmsTemplate.send(workloadQueue, session -> {
            TextMessage message = session.createTextMessage(json);
            message.setStringProperty(JmsConfig.TYPE_ID_PROPERTY, typeId);
            // The main service propagates its transaction id for tracing
            message.setStringProperty("transactionId", "cucumber-contract-tx");
            return message;
        });
    }
}
