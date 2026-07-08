package com.gymconnect.workload.repository;

import com.gymconnect.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link TrainerWorkload} documents.
 *
 * <p>Exposes the search and update operations the workload service needs:
 * {@link #findByUsername(String)} locates a trainer's summary document by username
 * (the document {@code _id}), and the inherited {@code save(...)} persists updates.</p>
 */
@Repository
public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    /**
     * Searches for a trainer's summary document by username.
     *
     * @param username the trainer username (document {@code _id})
     * @return the document, or empty if the trainer has no summary yet
     */
    Optional<TrainerWorkload> findByUsername(String username);
}
