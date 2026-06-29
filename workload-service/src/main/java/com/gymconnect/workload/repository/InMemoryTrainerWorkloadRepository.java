package com.gymconnect.workload.repository;

import com.gymconnect.workload.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory database for trainer workloads, keyed by username.
 *
 * <p>Backed by a {@link ConcurrentHashMap}; survives only for the lifetime of the
 * JVM, which matches the requirement to store the monthly summary "in an in-memory
 * database".</p>
 */
@Repository
public class InMemoryTrainerWorkloadRepository implements TrainerWorkloadRepository {

    private final Map<String, TrainerWorkload> store = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        store.put(workload.getUsername(), workload);
        return workload;
    }

    @Override
    public TrainerWorkload findOrCreate(String username, String firstName, String lastName,
                                        boolean active) {
        return store.compute(username, (key, existing) -> {
            if (existing == null) {
                return new TrainerWorkload(username, firstName, lastName, active);
            }
            existing.updateIdentity(firstName, lastName, active);
            return existing;
        });
    }
}
