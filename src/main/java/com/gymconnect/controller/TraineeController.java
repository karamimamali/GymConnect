package com.gymconnect.controller;

import com.gymconnect.dto.ActivateDeactivateRequest;
import com.gymconnect.dto.AddTrainingRequest;
import com.gymconnect.dto.CredentialsResponse;
import com.gymconnect.dto.TraineeProfileResponse;
import com.gymconnect.dto.TraineeRegistrationRequest;
import com.gymconnect.dto.TrainerSummary;
import com.gymconnect.dto.TrainingResponse;
import com.gymconnect.dto.UpdateTraineeRequest;
import com.gymconnect.dto.UpdateTraineeResponse;
import com.gymconnect.dto.UpdateTraineeTrainersRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee profile and training management")
public class TraineeController {

    private static final Logger logger = LoggerFactory.getLogger(TraineeController.class);

    private final GymFacade gymFacade;

    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(summary = "Register trainee", description = "Create a new trainee profile (public)")
    @ApiResponse(responseCode = "201", description = "Trainee registered successfully")
    public ResponseEntity<CredentialsResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request) {
        logger.info("Registering trainee: {} {}", request.firstName(), request.lastName());
        Trainee trainee = gymFacade.createTrainee(request.firstName(), request.lastName(),
                request.dateOfBirth(), request.address());
        CredentialsResponse response = new CredentialsResponse(
                trainee.getUser().getUsername(), trainee.getUser().getRawPassword());
        logger.info("Trainee registered with username: {}", response.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile", description = "Retrieve trainee profile by username")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        logger.debug("Getting trainee profile: {}", username);
        Trainee trainee = gymFacade.getTraineeByUsername(username);
        return ResponseEntity.ok(toProfileResponse(trainee));
    }

    @PutMapping
    @Operation(summary = "Update trainee profile", description = "Update an existing trainee profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UpdateTraineeResponse> updateProfile(
            @Valid @RequestBody UpdateTraineeRequest request) {
        logger.info("Updating trainee profile: {}", request.username());
        Trainee trainee = gymFacade.updateTrainee(request.username(),
                request.firstName(), request.lastName(), request.dateOfBirth(),
                request.address(), request.isActive());
        return ResponseEntity.ok(toUpdateResponse(trainee));
    }

    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile", description = "Hard delete trainee profile and cascade trainings")
    @ApiResponse(responseCode = "200", description = "Profile deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProfile(@PathVariable String username) {
        logger.info("Deleting trainee profile: {}", username);
        gymFacade.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/not-assigned-trainers")
    @Operation(summary = "Get not assigned active trainers",
            description = "Get active trainers not assigned to the trainee")
    @ApiResponse(responseCode = "200", description = "Trainers list retrieved")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainerSummary>> getNotAssignedTrainers(
            @PathVariable String username) {
        logger.debug("Getting unassigned trainers for trainee: {}", username);
        List<Trainer> trainers = gymFacade.getUnassignedTrainers(username);
        List<TrainerSummary> response = trainers.stream()
                .map(this::toTrainerSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    @Operation(summary = "Update trainee's trainer list",
            description = "Replace the trainee's assigned trainers list")
    @ApiResponse(responseCode = "200", description = "Trainers list updated")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainerSummary>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        logger.info("Updating trainers list for trainee: {}", username);
        gymFacade.updateTraineeTrainers(username, request.trainerUsernames());
        Trainee trainee = gymFacade.getTraineeByUsername(username);
        List<TrainerSummary> response = trainee.getTrainers().stream()
                .map(this::toTrainerSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee trainings list",
            description = "Retrieve trainee's training sessions with optional filters")
    @ApiResponse(responseCode = "200", description = "Trainings list retrieved")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {
        logger.debug("Getting trainings for trainee: {}", username);
        List<Training> trainings = gymFacade.getTraineeTrainings(username,
                periodFrom, periodTo, trainerName, trainingType);
        List<TrainingResponse> response = trainings.stream()
                .map(t -> new TrainingResponse(
                        t.getTrainingName(),
                        t.getTrainingDate(),
                        t.getTrainingType().getTrainingTypeName(),
                        t.getTrainingDuration(),
                        t.getTrainer().getUser().getFirstName() + " "
                                + t.getTrainer().getUser().getLastName()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{username}/trainings")
    @Operation(summary = "Add training", description = "Create a new training session")
    @ApiResponse(responseCode = "200", description = "Training added successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> addTraining(
            @PathVariable String username,
            @Valid @RequestBody AddTrainingRequest request) {
        logger.info("Adding training for trainee: {}", username);
        gymFacade.addTraining(username, request.trainerUsername(),
                request.trainingName(), request.trainingDate(),
                request.trainingDuration());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{username}")
    @Operation(summary = "Activate/Deactivate trainee",
            description = "Toggle trainee active status (non-idempotent)")
    @ApiResponse(responseCode = "200", description = "Status changed successfully")
    @ApiResponse(responseCode = "400", description = "Already in requested state")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> activateDeactivate(
            @PathVariable String username,
            @Valid @RequestBody ActivateDeactivateRequest request) {
        logger.info("Activate/deactivate trainee: {}, isActive: {}", username, request.isActive());
        if (request.isActive()) {
            gymFacade.activateTrainee(username);
        } else {
            gymFacade.deactivateTrainee(username);
        }
        return ResponseEntity.ok().build();
    }

    private TraineeProfileResponse toProfileResponse(Trainee trainee) {
        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(this::toTrainerSummary)
                .toList();
        return new TraineeProfileResponse(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainers);
    }

    private UpdateTraineeResponse toUpdateResponse(Trainee trainee) {
        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(this::toTrainerSummary)
                .toList();
        return new UpdateTraineeResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainers);
    }

    private TrainerSummary toTrainerSummary(Trainer trainer) {
        return new TrainerSummary(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization().getTrainingTypeName());
    }
}
