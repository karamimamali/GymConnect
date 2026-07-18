package com.gymconnect.workload.cucumber.steps;

import com.gymconnect.workload.cucumber.DeadLetterQueueProbe;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import jakarta.jms.Message;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Steps around the application dead letter queue, where the listener parks
 * events that parse but miss required information.
 */
public class DeadLetterSteps {

    private final DeadLetterQueueProbe dlqProbe;

    public DeadLetterSteps(DeadLetterQueueProbe dlqProbe) {
        this.dlqProbe = dlqProbe;
    }

    @Given("the dead letter queue is empty")
    public void theDeadLetterQueueIsEmpty() {
        dlqProbe.drain();
    }

    @Then("the event should be moved to the dead letter queue with a reason containing {string}")
    public void theEventShouldBeMovedToTheDlq(String reasonFragment) throws Exception {
        Message deadLetter = dlqProbe.receive();
        assertNotNull(deadLetter, "Expected a message on the dead letter queue");
        String reason = deadLetter.getStringProperty("rejectionReason");
        assertNotNull(reason, "Dead letters must carry a rejectionReason property");
        assertTrue(reason.contains(reasonFragment),
                () -> "Expected rejection reason to mention '" + reasonFragment
                        + "' but was: " + reason);
    }
}
