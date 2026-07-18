package com.gymconnect.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymconnect.cucumber.ScenarioState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Login, logout and password management steps — the non-functional security
 * requirements (JWT auth, brute force protection, token invalidation).
 */
public class AuthenticationSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioState state;

    public AuthenticationSteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioState state) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.state = state;
    }

    @Given("I am logged in as {string} {string}")
    public void iAmLoggedInAs(String firstName, String lastName) throws Exception {
        iLoginWithTheCredentialsOf(firstName, lastName);
        assertEquals(200, state.getLastStatus(),
                () -> "Login failed for " + firstName + " " + lastName + ": " + state.getLastBody());
        String token = objectMapper.readTree(state.getLastBody()).get("token").asText();
        state.setToken(token);
    }

    @When("I login with the credentials of {string} {string}")
    @When("I login as {string} {string} with the original password")
    public void iLoginWithTheCredentialsOf(String firstName, String lastName) throws Exception {
        ScenarioState.Credentials credentials = state.credentialsOf(firstName, lastName);
        login(credentials.username(), credentials.password());
    }

    @When("I login as {string} {string} with password {string}")
    public void iLoginAsWithPassword(String firstName, String lastName, String password)
            throws Exception {
        login(state.usernameOf(firstName, lastName), password);
    }

    @Given("I use an invalid token")
    public void iUseAnInvalidToken() {
        state.setToken("this.is-not.a-valid-jwt");
    }

    @When("I logout")
    public void iLogout() throws Exception {
        MvcResult result = mockMvc.perform(state.authorize(post("/api/users/logout"))).andReturn();
        state.record(result);
    }

    @When("I change the password of {string} {string} to {string}")
    public void iChangeThePasswordTo(String firstName, String lastName, String newPassword)
            throws Exception {
        changePassword(state.credentialsOf(firstName, lastName).password(), newPassword);
    }

    @When("I change the password of {string} {string} using old password {string} to {string}")
    public void iChangeThePasswordUsingOldPassword(String firstName, String lastName,
                                                   String oldPassword, String newPassword)
            throws Exception {
        changePassword(oldPassword, newPassword);
    }

    private void changePassword(String oldPassword, String newPassword) throws Exception {
        Map<String, Object> body = Map.of("oldPassword", oldPassword, "newPassword", newPassword);
        MvcResult result = mockMvc.perform(state.authorize(put("/api/users/change-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
    }

    private void login(String username, String password) throws Exception {
        Map<String, Object> body = Map.of("username", username, "password", password);
        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
    }
}
