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
import java.util.Map;

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

        if (request.actionType() == ActionType.ADD) {
            TrainerWorkload workload = repository.findOrCreate(request.username(),
                    request.firstName(), request.lastName(), request.active());
            workload.addDuration(year, month, request.trainingDuration());
            logger.debug("Accrued {} min to trainer '{}' for {}-{}",
                    request.trainingDuration(), request.username(), year, month);
        } else {
            repository.findByUsername(request.username()).ifPresentOrElse(workload -> {
                workload.updateIdentity(request.firstName(), request.lastName(), request.active());
                workload.subtractDuration(year, month, request.trainingDuration());
                logger.debug("Reversed {} min from trainer '{}' for {}-{}",
                        request.trainingDuration(), request.username(), year, month);
            }, () -> logger.warn("DELETE event ignored — no workload recorded for trainer '{}'",
                    request.username()));
        }
    }

    @Override
    public TrainerWorkloadSummaryResponse getSummary(String username) {
        logger.debug("Retrieving workload summary for trainer '{}'", username);
        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> new TrainerWorkloadNotFoundException(username));
        return toResponse(workload);
    }

    private TrainerWorkloadSummaryResponse toResponse(TrainerWorkload workload) {
        Map<Integer, Map<Integer, Integer>> snapshot = workload.snapshot();
        List<YearSummaryResponse> years = snapshot.entrySet().stream()
                .map(yearEntry -> new YearSummaryResponse(
                        yearEntry.getKey(),
                        yearEntry.getValue().entrySet().stream()
                                .map(monthEntry -> new MonthSummaryResponse(
                                        monthEntry.getKey(), monthEntry.getValue()))
                                .toList()))
                .toList();
        return new TrainerWorkloadSummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.isActive(),
                years);
    }
}
