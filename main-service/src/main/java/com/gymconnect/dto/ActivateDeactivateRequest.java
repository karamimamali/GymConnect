package com.gymconnect.dto;

import jakarta.validation.constraints.NotNull;

public record ActivateDeactivateRequest(
        @NotNull(message = "Is active is required") Boolean isActive
) {
}
