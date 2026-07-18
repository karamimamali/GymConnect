package com.gymconnect.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.cucumber.ScenarioState;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generic response assertions shared by every feature of the main service.
 */
public class CommonSteps {

    private final ScenarioState state;
    private final ObjectMapper objectMapper;

    public CommonSteps(ScenarioState state, ObjectMapper objectMapper) {
        this.state = state;
        this.objectMapper = objectMapper;
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, state.getLastStatus(),
                () -> "Unexpected status; response body was: " + state.getLastBody());
    }

    @Then("the response should contain error {string}")
    public void theResponseShouldContainError(String message) {
        assertTrue(state.getLastBody().contains(message),
                () -> "Expected error '" + message + "' but body was: " + state.getLastBody());
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String text) {
        assertTrue(state.getLastBody().contains(text),
                () -> "Expected body to contain '" + text + "' but was: " + state.getLastBody());
    }

    @Then("the response should not contain {string}")
    public void theResponseShouldNotContain(String text) {
        assertFalse(state.getLastBody().contains(text),
                () -> "Expected body NOT to contain '" + text + "' but was: " + state.getLastBody());
    }

    @Then("the response should contain generated credentials")
    public void theResponseShouldContainGeneratedCredentials() throws Exception {
        JsonNode body = objectMapper.readTree(state.getLastBody());
        assertTrue(body.hasNonNull("username") && !body.get("username").asText().isBlank(),
                "Expected a generated username in: " + state.getLastBody());
        assertTrue(body.hasNonNull("password") && !body.get("password").asText().isBlank(),
                "Expected a generated password in the registration response");
    }

    @Then("the response should contain a JWT token")
    public void theResponseShouldContainAJwtToken() throws Exception {
        JsonNode body = objectMapper.readTree(state.getLastBody());
        assertTrue(body.hasNonNull("token") && body.get("token").asText().split("\\.").length == 3,
                "Expected a three-part JWT in: " + state.getLastBody());
    }
}
