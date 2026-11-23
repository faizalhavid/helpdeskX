package com.helpdesk.api.incidents.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignIncidentRequest(
        @NotNull UUID userId
) {
}
