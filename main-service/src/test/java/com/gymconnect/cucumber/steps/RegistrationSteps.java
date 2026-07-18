package com.gymconnect.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.cucumber.ScenarioState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Registration of trainees and trainers. Successful registrations store the
 * generated credentials in the scenario state so later steps can log in or
 * resolve usernames without hard-coding generated values.
 */
public class RegistrationSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioState state;

    public RegistrationSteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioState state) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.state = state;
    }

    @When("I register a trainee with first name {string} and last name {string}")
    public void iRegisterATrainee(String firstName, String lastName) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("dateOfBirth", "1995-04-12");
        body.put("address", "1 Test Street");
        MvcResult result = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
        rememberCredentialsIfCreated(firstName, lastName);
    }

    @Given("a registered trainee {string} {string}")
    public void aRegisteredTrainee(String firstName, String lastName) throws Exception {
        iRegisterATrainee(firstName, lastName);
        assertEquals(201, state.getLastStatus(),
                () -> "Trainee registration failed: " + state.getLastBody());
    }

    @When("I register a trainer with first name {string} and last name {string} specialized in {string}")
    public void iRegisterATrainer(String firstName, String lastName, String specialization)
            throws Exception {
        Map<String, Object> body = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "specialization", specialization);
        MvcResult result = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
        rememberCredentialsIfCreated(firstName, lastName);
    }

    @Given("a registered trainer {string} {string} specialized in {string}")
    public void aRegisteredTrainer(String firstName, String lastName, String specialization)
            throws Exception {
        iRegisterATrainer(firstName, lastName, specialization);
        assertEquals(201, state.getLastStatus(),
                () -> "Trainer registration failed: " + state.getLastBody());
    }

    private void rememberCredentialsIfCreated(String firstName, String lastName) throws Exception {
        if (state.getLastStatus() != 201) {
            return;
        }
        JsonNode body = objectMapper.readTree(state.getLastBody());
        state.rememberUser(firstName, lastName, new ScenarioState.Credentials(
                body.get("username").asText(), body.get("password").asText()));
    }
}
