package com.gymconnect.controller;

import com.gymconnect.dto.ActivateDeactivateRequest;
import com.gymconnect.dto.CredentialsResponse;
import com.gymconnect.dto.TraineeSummary;
import com.gymconnect.dto.TrainerProfileResponse;
import com.gymconnect.dto.TrainerRegistrationRequest;
import com.gymconnect.dto.TrainerTrainingResponse;
import com.gymconnect.dto.UpdateTrainerRequest;
import com.gymconnect.dto.UpdateTrainerResponse;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.model.Trainee;
import com.gymconnect.model.Trainer;
import com.gymconnect.model.Training;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer", description = "Trainer profile and training management")
public class TrainerController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;

    public TrainerController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(summary = "Register trainer", description = "Create a new trainer profile (public)")
    @ApiResponse(responseCode = "201", description = "Trainer registered successfully")
    public ResponseEntity<CredentialsResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        logger.info("Registering trainer: {} {}", request.firstName(), request.lastName());
        Trainer trainer = gymFacade.createTrainer(request.firstName(), request.lastName(),
                request.specialization());
        CredentialsResponse response = new CredentialsResponse(
                trainer.getUser().getUsername(), trainer.getUser().getRawPassword());
        logger.info("Trainer registered with username: {}", response.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile", description = "Retrieve trainer profile by username")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        logger.debug("Getting trainer profile: {}", username);
        Trainer trainer = gymFacade.getTrainerByUsername(username);
        return ResponseEntity.ok(toProfileResponse(trainer));
    }

    @PutMapping
    @Operation(summary = "Update trainer profile",
            description = "Update an existing trainer profile (specialization is read-only)")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UpdateTrainerResponse> updateProfile(
            @Valid @RequestBody UpdateTrainerRequest request) {
        logger.info("Updating trainer profile: {}", request.username());
        Trainer existing = gymFacade.getTrainerByUsername(request.username());
        String specialization = existing.getSpecialization().getTrainingTypeName();
        Trainer trainer = gymFacade.updateTrainer(request.username(),
                request.firstName(), request.lastName(), specialization, request.isActive());
        return ResponseEntity.ok(toUpdateResponse(trainer));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings list",
            description = "Retrieve trainer's training sessions with optional filters")
    @ApiResponse(responseCode = "200", description = "Trainings list retrieved")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {
        logger.debug("Getting trainings for trainer: {}", username);
        List<Training> trainings = gymFacade.getTrainerTrainings(username,
                periodFrom, periodTo, traineeName);
        List<TrainerTrainingResponse> response = trainings.stream()
                .map(t -> new TrainerTrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType().getTrainingTypeName(),
                        t.getTrainingDuration(),
                        t.getTrainee().getUser().getFirstName() + " "
                                + t.getTrainee().getUser().getLastName()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate/Deactivate trainer",
            description = "Toggle trainer active status (non-idempotent)")
    @ApiResponse(responseCode = "200", description = "Status changed successfully")
    @ApiResponse(responseCode = "400", description = "Already in requested state")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> activateDeactivate(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        logger.info("Activate/deactivate trainer: {}, isActive: {}", username, request.isActive());
        if (request.isActive()) {
            gymFacade.activateTrainer(username);
        } else {
            gymFacade.deactivateTrainer(username);
        }
        return ResponseEntity.ok().build();
    }

    private TrainerProfileResponse toProfileResponse(Trainer trainer) {
        List<TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(this::toTraineeSummary)
                .toList();
        return new TrainerProfileResponse(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().getIsActive(),
                trainees);
    }

    private UpdateTrainerResponse toUpdateResponse(Trainer trainer) {
        List<TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(this::toTraineeSummary)
                .toList();
        return new UpdateTrainerResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                trainer.getUser().getIsActive(),
                trainees);
    }

    private TraineeSummary toTraineeSummary(Trainee trainee) {
        return new TraineeSummary(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName());
    }
}
