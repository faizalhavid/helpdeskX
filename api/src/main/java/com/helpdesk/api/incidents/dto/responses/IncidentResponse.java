package com.helpdesk.api.incidents.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import com.helpdesk.api.incidents.entities.enums.IncidentPriority;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;

public record IncidentResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        @JsonProperty("ticket_number") String ticketNumber,
        String title,
        String description,
        CategoryResponse category,
        IncidentPriority priority,
        IncidentStatus status,
        @JsonProperty("assigned_to") UserProfileResponse assignedTo
) {
}
