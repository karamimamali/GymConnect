package com.gymconnect.workload.repository;

import com.gymconnect.workload.model.TrainerWorkload;

import java.util.Optional;

/**
 * Persistence abstraction for trainer workloads. The default implementation is an
 * in-memory store, but the interface keeps the service layer decoupled from that
 * choice (e.g. it could be swapped for a real database later).
 */
public interface TrainerWorkloadRepository {

    Optional<TrainerWorkload> findByUsername(String username);

    TrainerWorkload save(TrainerWorkload workload);

    /**
     * Returns the existing workload for {@code username}, or creates, stores and
     * returns a new one initialised with the supplied identity fields.
     */
    TrainerWorkload findOrCreate(String username, String firstName, String lastName, boolean active);
}
