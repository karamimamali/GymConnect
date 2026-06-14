package com.gymconnect.controller;

import com.gymconnect.dto.ChangePasswordRequest;
import com.gymconnect.dto.LoginRequest;
import com.gymconnect.dto.LoginResponse;
import com.gymconnect.facade.GymFacade;
import com.gymconnect.security.LoginAttemptService;
import com.gymconnect.security.JwtTokenProvider;
import com.gymconnect.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "Login, logout and password management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final GymFacade gymFacade;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(GymFacade gymFacade,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          LoginAttemptService loginAttemptService,
                          TokenBlacklistService tokenBlacklistService) {
        this.gymFacade = gymFacade;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username/password and receive a JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful – JWT token returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @ApiResponse(responseCode = "423", description = "Account locked due to too many failed attempts")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.username();
        logger.info("Login attempt for user: {}", username);

        if (loginAttemptService.isBlocked(username)) {
            logger.warn("Login blocked for user due to too many failed attempts: {}", username);
            throw new LockedException("Account temporarily blocked. Please try again in 5 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));

            loginAttemptService.loginSucceeded(username);
            String token = jwtTokenProvider.generateToken(authentication.getName());
            logger.info("Login successful for user: {}", username);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(username);
            logger.warn("Login failed for user: {}", username);
            throw new SecurityException("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate the current JWT token")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            tokenBlacklistService.blacklist(token);
            logger.info("User logged out successfully");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the authenticated user's password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid old password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               Authentication authentication) {
        String username = authentication.getName();
        logger.info("Password change request for user: {}", username);
        gymFacade.changePassword(username, request.oldPassword(), request.newPassword());
        logger.info("Password changed successfully for user: {}", username);
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
