package com.helpdesk.api.incidents.dto.requests;

import com.helpdesk.api.incidents.entities.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;


public record UpdateIncidentStatusRequest(
        @NotNull
        IncidentStatus status
) { }