package com.gymconnect.workload.service;

import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;

/**
 * Calculates and serves trainers' monthly training summaries.
 */
public interface TrainerWorkloadService {

    /** Applies an ADD/DELETE workload event to the trainer's monthly totals. */
    void process(TrainerWorkloadRequest request);

    /**
     * @return the full year/month summary for the trainer
     * @throws com.gymconnect.workload.exception.TrainerWorkloadNotFoundException
     *         if no data exists for the trainer
     */
    TrainerWorkloadSummaryResponse getSummary(String username);
}
