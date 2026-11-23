package com.helpdesk.api.incidents.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.helpdesk.api.incidents.entities.enums.IncidentPriority;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IncidentRequest(
        @NotBlank @JsonProperty("ticket_number") String ticketNumber,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @JsonProperty("category_id") UUID categoryId,
        @NotNull IncidentPriority priority
) {
}
