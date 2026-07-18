package com.gymconnect.workload.cucumber.steps;

import com.gymconnect.workload.cucumber.DeadLetterQueueProbe;
import com.gymconnect.workload.repository.TrainerWorkloadRepository;
import io.cucumber.java.Before;

/**
 * Resets shared infrastructure between scenarios so every scenario starts from
 * a clean database and empty dead letter queue (test isolation).
 */
public class Hooks {

    private final TrainerWorkloadRepository repository;
    private final DeadLetterQueueProbe dlqProbe;

    public Hooks(TrainerWorkloadRepository repository, DeadLetterQueueProbe dlqProbe) {
        this.repository = repository;
        this.dlqProbe = dlqProbe;
    }

    @Before
    public void resetState() {
        repository.deleteAll();
        dlqProbe.drain();
    }
}
