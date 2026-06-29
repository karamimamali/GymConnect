package com.gymconnect.workload.controller;

import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import com.gymconnect.workload.dto.TrainerWorkloadSummaryResponse;
import com.gymconnect.workload.service.TrainerWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workloads")
@Tag(name = "Trainer Workload", description = "Accrual and retrieval of trainers' monthly training hours")
@SecurityRequirement(name = "bearerAuth")
public class TrainerWorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadController.class);

    private final TrainerWorkloadService workloadService;

    public TrainerWorkloadController(TrainerWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    @Operation(summary = "Submit a workload event",
            description = "Add or remove a training session from a trainer's monthly total")
    @ApiResponse(responseCode = "200", description = "Workload event processed")
    @ApiResponse(responseCode = "400", description = "Invalid workload payload")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    public ResponseEntity<Void> submitWorkload(@Valid @RequestBody TrainerWorkloadRequest request) {
        logger.info("Received {} workload for trainer '{}'", request.actionType(), request.username());
        workloadService.process(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get a trainer's monthly workload summary",
            description = "Return the trainer's accumulated training duration grouped by year and month")
    @ApiResponse(responseCode = "200", description = "Summary retrieved")
    @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token")
    @ApiResponse(responseCode = "404", description = "No workload recorded for the trainer")
    public ResponseEntity<TrainerWorkloadSummaryResponse> getSummary(@PathVariable String username) {
        logger.info("Workload summary requested for trainer '{}'", username);
        return ResponseEntity.ok(workloadService.getSummary(username));
    }
}
