package com.gymconnect.workload.service;

import com.gymconnect.workload.dto.ActionType;
import com.gymconnect.workload.dto.MonthSummaryResponse;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;
import com.gymconnect.workload.dto.YearSummaryResponse;
import com.gymconnect.workload.exception.TrainerWorkloadNotFoundException;
import com.gymconnect.workload.model.TrainerWorkload;
import com.gymconnect.workload.repository.TrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Applies incoming workload events to trainers' MongoDB training summaries and serves
 * those summaries on request.
 *
 * <p>Logging is two-tiered: the transaction boundary is logged by the JMS listener
 * under the propagated {@code transactionId}, while this service logs each business
 * operation (extract / create / accrue / save) at DEBUG so a single transaction can be
 * followed step by step.</p>
 */
@Service
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadServiceImpl.class);

    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadServiceImpl(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public void process(TrainerWorkloadRequest request) {
        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();
        logger.info("Processing {} event for trainer '{}' on {}-{}, duration={} min",
                request.actionType(), request.username(), year, month, request.trainingDuration());

        if (request.actionType() == ActionType.DELETE) {
            reverse(request, year, month);
        } else {
            accrue(request, year, month);
        }
    }

    /** Steps a–e: extract the trainer document, create or update the year/month record, save. */
    private void accrue(TrainerWorkloadRequest request, int year, int month) {
        TrainerWorkload workload = repository.findByUsername(request.username())
                .map(existing -> {
                    logger.debug("Extracted existing summary for trainer '{}'", request.username());
                    existing.updateIdentity(request.firstName(), request.lastName(), request.active());
                    return existing;
                })
                .orElseGet(() -> {
                    logger.debug("No summary found — creating new document for trainer '{}'",
                            request.username());
                    return new TrainerWorkload(request.username(), request.firstName(),
                            request.lastName(), request.active());
                });

        workload.addDuration(year, month, request.trainingDuration());
        repository.save(workload);
        logger.debug("Saved trainer '{}': added {} min to {}-{}",
                request.username(), request.trainingDuration(), year, month);
    }

    /** Reverses a previously accrued duration; a no-op when the trainer is unknown. */
    private void reverse(TrainerWorkloadRequest request, int year, int month) {
        repository.findByUsername(request.username()).ifPresentOrElse(workload -> {
            workload.updateIdentity(request.firstName(), request.lastName(), request.active());
            workload.subtractDuration(year, month, request.trainingDuration());
            repository.save(workload);
            logger.debug("Saved trainer '{}': reversed {} min from {}-{}",
                    request.username(), request.trainingDuration(), year, month);
        }, () -> logger.warn("DELETE event ignored — no summary recorded for trainer '{}'",
                request.username()));
    }

    @Override
    public TrainerWorkloadSummaryResponse getSummary(String username) {
        logger.debug("Retrieving workload summary for trainer '{}'", username);
        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> new TrainerWorkloadNotFoundException(username));
        return toResponse(workload);
    }

    private TrainerWorkloadSummaryResponse toResponse(TrainerWorkload workload) {
        List<YearSummaryResponse> years = workload.getYears().stream()
                .map(year -> new YearSummaryResponse(
                        year.getYear(),
                        year.getMonths().stream()
                                .map(month -> new MonthSummaryResponse(
                                        month.getMonth(), month.getTrainingSummaryDuration()))
                                .toList()))
                .toList();
        return new TrainerWorkloadSummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                Boolean.TRUE.equals(workload.getActive()),
                years);
    }
}
