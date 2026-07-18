package com.gymconnect.workload.cucumber.steps;

import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;

import java.time.Duration;
import java.time.LocalDate;

import static org.awaitility.Awaitility.await;

/**
 * Sends trainer workload events through the real ActiveMQ queue, exactly as
 * the main service does, and synchronises with the asynchronous listener.
 */
public class WorkloadEventSteps {

    /** Sentinel trainer used only to detect that the queue has been drained. */
    private static final String BARRIER_USERNAME = "Sync.Barrier";

    private final JmsTemplate jmsTemplate;
    private final TrainerWorkloadRepository repository;
    private final String workloadQueue;

    public WorkloadEventSteps(JmsTemplate jmsTemplate,
                              TrainerWorkloadRepository repository,
                              @Value("${app.jms.workload-queue}") String workloadQueue) {
        this.jmsTemplate = jmsTemplate;
        this.repository = repository;
        this.workloadQueue = workloadQueue;
    }

    @Given("no workload has been recorded yet")
    public void noWorkloadHasBeenRecordedYet() {
        repository.deleteAll();
    }

    @When("a/an {string} workload event of {int} minutes on {string} is received for trainer {string}")
    public void aWorkloadEventIsReceived(String action, int minutes, String date, String username) {
        // Trainer usernames follow the First.Last convention of the main service
        String[] name = username.split("\\.", 2);
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                username, name[0], name[1], true,
                LocalDate.parse(date), minutes, ActionType.valueOf(action));
        jmsTemplate.convertAndSend(workloadQueue, request);
    }

    @When("a workload event with a blank username is received")
    public void aWorkloadEventWithABlankUsernameIsReceived() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "", "Ghost", "Writer", true, LocalDate.of(2026, 7, 1), 30, ActionType.ADD);
        jmsTemplate.convertAndSend(workloadQueue, request);
    }

    /**
     * Deterministic barrier for the asynchronous listener: with a single
     * consumer the queue is processed in order, so once a trailing sentinel
     * event has been applied every earlier event has been applied too.
     */
    @When("all workload events have been processed")
    public void allWorkloadEventsHaveBeenProcessed() {
        aWorkloadEventIsReceived("ADD", 1, "2000-01-01", BARRIER_USERNAME);
        await().atMost(Duration.ofSeconds(10))
                .until(() -> repository.findByUsername(BARRIER_USERNAME).isPresent());
        repository.deleteById(BARRIER_USERNAME);
    }
}
