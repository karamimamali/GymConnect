package com.gymconnect.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.config.JmsConfig;
import com.gymconnect.cucumber.ScenarioState;
import com.gymconnect.cucumber.WorkloadQueueProbe;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Steps asserting on the messages the main service publishes for the workload
 * microservice — the outbound half of the inter-service integration contract.
 */
public class WorkloadMessageSteps {

    private final WorkloadQueueProbe queueProbe;
    private final ScenarioState state;
    private final ObjectMapper objectMapper;

    public WorkloadMessageSteps(WorkloadQueueProbe queueProbe, ScenarioState state,
                                ObjectMapper objectMapper) {
        this.queueProbe = queueProbe;
        this.state = state;
        this.objectMapper = objectMapper;
    }

    @Given("the workload queue is empty")
    public void theWorkloadQueueIsEmpty() {
        queueProbe.drain();
    }

    @Then("a workload message should be published")
    public void aWorkloadMessageShouldBePublished() {
        Message message = queueProbe.receive();
        assertNotNull(message, "Expected a message on the trainer workload queue");
        state.setLastWorkloadMessage(message);
    }

    @Then("the workload message should have type id {string}")
    public void theWorkloadMessageShouldHaveTypeId(String typeId) throws Exception {
        assertEquals(typeId,
                state.getLastWorkloadMessage().getStringProperty(JmsConfig.TYPE_ID_PROPERTY),
                "The consumer resolves the payload type from this JMS property");
    }

    @Then("the workload message should have action {string}, duration {int} and training date {string}")
    public void theWorkloadMessageShouldHaveActionDurationAndDate(String action, int duration,
                                                                  String date) throws Exception {
        JsonNode payload = payload();
        assertEquals(action, payload.get("actionType").asText());
        assertEquals(duration, payload.get("trainingDuration").asInt());
        assertEquals(date, payload.get("trainingDate").asText(),
                "Training date must travel as an ISO-8601 string");
    }

    @Then("the workload message should be for the trainer {string} {string}")
    public void theWorkloadMessageShouldBeForTheTrainer(String firstName, String lastName)
            throws Exception {
        JsonNode payload = payload();
        assertEquals(state.usernameOf(firstName, lastName), payload.get("username").asText());
        assertEquals(firstName, payload.get("firstName").asText());
        assertEquals(lastName, payload.get("lastName").asText());
    }

    /** Messages travel as JSON text so the consumer needs none of our classes. */
    private JsonNode payload() throws Exception {
        Message message = state.getLastWorkloadMessage();
        assertNotNull(message, "No workload message captured — "
                + "did the scenario run the 'a workload message should be published' step?");
        TextMessage textMessage = assertInstanceOf(TextMessage.class, message,
                "Workload events must be TextMessages carrying JSON");
        return objectMapper.readTree(textMessage.getText());
    }
}
