package com.gymconnect.workload.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.workload.cucumber.ScenarioState;
import com.gymconnect.workload.cucumber.ServiceTokenFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * REST retrieval of trainers' workload summaries, including the JWT-based
 * service-to-service authentication (non-functional requirement).
 */
public class SummarySteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioState state;
    private final ServiceTokenFactory tokenFactory;

    public SummarySteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioState state,
                        ServiceTokenFactory tokenFactory) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.state = state;
        this.tokenFactory = tokenFactory;
    }

    @Given("I have a service token issued with the shared secret")
    public void iHaveAServiceTokenIssuedWithTheSharedSecret() {
        state.setToken(tokenFactory.issuedWithSharedSecret("main-service"));
    }

    @Given("I have a service token signed with an unknown key")
    public void iHaveAServiceTokenSignedWithAnUnknownKey() {
        state.setToken(tokenFactory.issuedWithForeignKey("main-service"));
    }

    @When("I request the workload summary of trainer {string}")
    public void iRequestTheWorkloadSummaryOfTrainer(String username) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/workloads/{username}", username);
        if (state.getToken() != null) {
            request.header("Authorization", "Bearer " + state.getToken());
        }
        MvcResult result = mockMvc.perform(request).andReturn();
        state.record(result);
    }

    @When("I request the workload summary of trainer {string} without a token")
    public void iRequestTheWorkloadSummaryWithoutAToken(String username) throws Exception {
        state.setToken(null);
        iRequestTheWorkloadSummaryOfTrainer(username);
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

    @Then("the summary should be for trainer {string} named {string} {string}")
    public void theSummaryShouldBeForTrainer(String username, String firstName, String lastName)
            throws Exception {
        JsonNode body = objectMapper.readTree(state.getLastBody());
        assertEquals(username, body.get("username").asText());
        assertEquals(firstName, body.get("firstName").asText());
        assertEquals(lastName, body.get("lastName").asText());
    }

    @Then("the summary should show {long} minutes for month {int} of year {int}")
    public void theSummaryShouldShowMinutes(long minutes, int month, int year) throws Exception {
        JsonNode years = objectMapper.readTree(state.getLastBody()).get("years");
        for (JsonNode yearNode : years) {
            if (yearNode.get("year").asInt() != year) {
                continue;
            }
            for (JsonNode monthNode : yearNode.get("months")) {
                if (monthNode.get("month").asInt() == month) {
                    assertEquals(minutes, monthNode.get("trainingSummaryDuration").asLong(),
                            "Wrong accumulated duration for " + year + "-" + month);
                    return;
                }
            }
        }
        fail("No summary entry for " + year + "-" + month + " in: " + state.getLastBody());
    }

    @Then("the summary should have no recorded years")
    public void theSummaryShouldHaveNoRecordedYears() throws Exception {
        JsonNode years = objectMapper.readTree(state.getLastBody()).get("years");
        assertTrue(years.isEmpty(),
                () -> "Expected an empty years list but was: " + state.getLastBody());
    }
}
