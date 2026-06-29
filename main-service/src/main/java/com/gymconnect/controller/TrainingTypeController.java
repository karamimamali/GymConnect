package com.gymconnect.controller;

import com.gymconnect.dao.TrainingTypeDao;
import com.gymconnect.dto.TrainingTypeResponse;
import com.gymconnect.model.TrainingType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Type", description = "Training type reference data")
public class TrainingTypeController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeController.class);

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeController(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Get training types", description = "Get all available training types")
    @ApiResponse(responseCode = "200", description = "Training types retrieved")
    public ResponseEntity<List<TrainingTypeResponse>> getAll() {
        logger.debug("Getting all training types");
        List<TrainingType> types = trainingTypeDao.findAll();
        List<TrainingTypeResponse> response = types.stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .toList();
        return ResponseEntity.ok(response);
    }
}
