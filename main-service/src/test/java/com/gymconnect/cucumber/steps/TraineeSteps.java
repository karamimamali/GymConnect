package com.gymconnect.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.cucumber.ScenarioState;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Trainee profile management steps (retrieve, update, activate, delete).
 */
public class TraineeSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioState state;

    public TraineeSteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioState state) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.state = state;
    }

    @When("I request the trainee profile of {string} {string}")
    public void iRequestTheTraineeProfileOf(String firstName, String lastName) throws Exception {
        requestProfile(state.usernameOf(firstName, lastName), true);
    }

    @When("I request the trainee profile of {string} {string} without a token")
    public void iRequestTheTraineeProfileWithoutAToken(String firstName, String lastName)
            throws Exception {
        requestProfile(state.usernameOf(firstName, lastName), false);
    }

    @When("I request the trainee profile for username {string}")
    public void iRequestTheTraineeProfileForUsername(String username) throws Exception {
        requestProfile(username, true);
    }

    @When("I update the trainee {string} {string} with last name {string} and address {string}")
    public void iUpdateTheTrainee(String firstName, String lastName,
                                  String newLastName, String address) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("username", state.usernameOf(firstName, lastName));
        body.put("firstName", firstName);
        body.put("lastName", newLastName);
        body.put("address", address);
        body.put("isActive", true);
        MvcResult result = mockMvc.perform(state.authorize(put("/api/trainees"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
    }

    @When("I set the active status of trainee {string} {string} to {}")
    public void iSetTheActiveStatusOfTrainee(String firstName, String lastName, String isActive)
            throws Exception {
        Map<String, Object> body = Map.of("isActive", Boolean.parseBoolean(isActive));
        MvcResult result = mockMvc.perform(
                        state.authorize(patch("/api/trainees/{username}",
                                        state.usernameOf(firstName, lastName)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
    }

    @When("I delete the trainee {string} {string}")
    public void iDeleteTheTrainee(String firstName, String lastName) throws Exception {
        MvcResult result = mockMvc.perform(
                        state.authorize(delete("/api/trainees/{username}",
                                state.usernameOf(firstName, lastName))))
                .andReturn();
        state.record(result);
    }

    private void requestProfile(String username, boolean authorized) throws Exception {
        var request = get("/api/trainees/{username}", username);
        MvcResult result = mockMvc.perform(authorized ? state.authorize(request) : request)
                .andReturn();
        state.record(result);
    }
}
