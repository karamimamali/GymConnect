package com.gymconnect.controller;

import com.gymconnect.dto.ChangePasswordRequest;
import com.gymconnect.facade.GymFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Login and password management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final GymFacade gymFacade;

    public UserController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping("/login")
    @Operation(summary = "Login", description = "Validate user credentials")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> login(@RequestParam String username,
                                      @RequestParam String password) {
        logger.info("Login attempt for user: {}", username);
        boolean authenticated = gymFacade.authenticateTrainee(username, password)
                || gymFacade.authenticateTrainer(username, password);
        if (!authenticated) {
            throw new SecurityException("Invalid credentials");
        }
        logger.info("Login successful for user: {}", username);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        logger.info("Password change request for user: {}", request.username());
        boolean isTrainee = gymFacade.authenticateTrainee(request.username(), request.oldPassword());
        boolean isTrainer = gymFacade.authenticateTrainer(request.username(), request.oldPassword());

        if (!isTrainee && !isTrainer) {
            throw new SecurityException("Invalid credentials");
        }

        if (isTrainee) {
            gymFacade.changeTraineePassword(request.username(), request.oldPassword(),
                    request.newPassword());
        } else {
            gymFacade.changeTrainerPassword(request.username(), request.oldPassword(),
                    request.newPassword());
        }

        logger.info("Password changed successfully for user: {}", request.username());
        return ResponseEntity.ok().build();
    }
}
