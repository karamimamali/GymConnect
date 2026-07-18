package com.gymconnect.cucumber.steps;

import com.gymconnect.cucumber.ScenarioState;
import io.cucumber.java.en.When;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Trainer profile and training-type retrieval steps.
 */
public class TrainerSteps {

    private final MockMvc mockMvc;
    private final ScenarioState state;

    public TrainerSteps(MockMvc mockMvc, ScenarioState state) {
        this.mockMvc = mockMvc;
        this.state = state;
    }

    @When("I request the trainer profile of {string} {string}")
    public void iRequestTheTrainerProfileOf(String firstName, String lastName) throws Exception {
        requestProfile(state.usernameOf(firstName, lastName), true);
    }

    @When("I request the trainer profile of {string} {string} without a token")
    public void iRequestTheTrainerProfileWithoutAToken(String firstName, String lastName)
            throws Exception {
        requestProfile(state.usernameOf(firstName, lastName), false);
    }

    @When("I request the list of training types")
    public void iRequestTheListOfTrainingTypes() throws Exception {
        MvcResult result = mockMvc.perform(state.authorize(get("/api/training-types"))).andReturn();
        state.record(result);
    }

    private void requestProfile(String username, boolean authorized) throws Exception {
        var request = get("/api/trainers/{username}", username);
        MvcResult result = mockMvc.perform(authorized ? state.authorize(request) : request)
                .andReturn();
        state.record(result);
    }
}
