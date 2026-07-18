package com.gymconnect.cucumber.steps;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Training creation and retrieval steps.
 */
public class TrainingSteps {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final ScenarioState state;

    public TrainingSteps(MockMvc mockMvc, ObjectMapper objectMapper, ScenarioState state) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.state = state;
    }

    @When("I add a training named {string} on {string} lasting {int} minutes for {string} {string} with trainer {string} {string}")
    public void iAddATraining(String name, String date, int duration,
                              String traineeFirst, String traineeLast,
                              String trainerFirst, String trainerLast) throws Exception {
        addTraining(name, date, duration,
                state.usernameOf(traineeFirst, traineeLast),
                state.usernameOf(trainerFirst, trainerLast));
    }

    @Given("a training named {string} on {string} lasting {int} minutes exists for {string} {string} with trainer {string} {string}")
    public void aTrainingExists(String name, String date, int duration,
                                String traineeFirst, String traineeLast,
                                String trainerFirst, String trainerLast) throws Exception {
        iAddATraining(name, date, duration, traineeFirst, traineeLast, trainerFirst, trainerLast);
        assertEquals(200, state.getLastStatus(),
                () -> "Training setup failed: " + state.getLastBody());
    }

    @When("I add a training named {string} on {string} without a duration for {string} {string} with trainer {string} {string}")
    public void iAddATrainingWithoutADuration(String name, String date,
                                              String traineeFirst, String traineeLast,
                                              String trainerFirst, String trainerLast)
            throws Exception {
        addTraining(name, date, null,
                state.usernameOf(traineeFirst, traineeLast),
                state.usernameOf(trainerFirst, trainerLast));
    }

    @When("I add a training named {string} on {string} lasting {int} minutes for {string} {string} with trainer username {string}")
    public void iAddATrainingWithTrainerUsername(String name, String date, int duration,
                                                 String traineeFirst, String traineeLast,
                                                 String trainerUsername) throws Exception {
        addTraining(name, date, duration,
                state.usernameOf(traineeFirst, traineeLast), trainerUsername);
    }

    @When("I request the trainings of trainee {string} {string}")
    public void iRequestTheTrainingsOfTrainee(String firstName, String lastName) throws Exception {
        MvcResult result = mockMvc.perform(
                        state.authorize(get("/api/trainees/{username}/trainings",
                                state.usernameOf(firstName, lastName))))
                .andReturn();
        state.record(result);
    }

    @When("I request the trainings of trainee {string} {string} between {string} and {string}")
    public void iRequestTheTrainingsOfTraineeBetween(String firstName, String lastName,
                                                     String from, String to) throws Exception {
        MvcResult result = mockMvc.perform(
                        state.authorize(get("/api/trainees/{username}/trainings",
                                        state.usernameOf(firstName, lastName))
                                .param("periodFrom", from)
                                .param("periodTo", to)))
                .andReturn();
        state.record(result);
    }

    private void addTraining(String name, String date, Integer duration,
                             String traineeUsername, String trainerUsername) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("traineeUsername", traineeUsername);
        body.put("trainerUsername", trainerUsername);
        body.put("trainingName", name);
        body.put("trainingDate", date);
        if (duration != null) {
            body.put("trainingDuration", duration);
        }
        MvcResult result = mockMvc.perform(
                        state.authorize(post("/api/trainees/{username}/trainings", traineeUsername))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andReturn();
        state.record(result);
    }
}
