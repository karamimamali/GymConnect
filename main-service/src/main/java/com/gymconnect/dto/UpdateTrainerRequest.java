package com.gymconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainerRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotNull(message = "Is active is required") Boolean isActive
) {
}
