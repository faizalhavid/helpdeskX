package com.helpdesk.api.incidents.dto.requests;

import com.helpdesk.api.incidents.entities.enums.IncidentPriority;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentFilterRequest(
        IncidentStatus status,
        IncidentPriority priority,
        String keyword,
        String categoryId,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
