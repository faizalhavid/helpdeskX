package com.helpdesk.api.incidents.services;

import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.models.GenericSpec;
import com.helpdesk.api.incidents.dto.requests.IncidentFilterRequest;
import com.helpdesk.api.incidents.dto.requests.IncidentRequest;
import com.helpdesk.api.incidents.dto.responses.IncidentResponse;
import com.helpdesk.api.incidents.entities.Category;
import com.helpdesk.api.incidents.entities.Incident;
import com.helpdesk.api.incidents.entities.enums.IncidentStatus;
import com.helpdesk.api.incidents.mappers.IncidentMapper;
import com.helpdesk.api.incidents.repositories.CategoryRepository;
import com.helpdesk.api.incidents.repositories.IncidentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IncidentMapper incidentMapper;

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = Map.of(
            IncidentStatus.NEW, Set.of(IncidentStatus.ASSIGNED),
            IncidentStatus.ASSIGNED, Set.of(IncidentStatus.IN_PROGRESS),
            IncidentStatus.IN_PROGRESS, Set.of(IncidentStatus.PENDING, IncidentStatus.RESOLVED)
    );

    public IncidentResponse createIncident(IncidentRequest incidentRequest) {
//        UUID uuidAssignedTo = parseUuid(incidentRequest.a());
//        User assignedUser = userRepository.findById(uuidAssignedTo)
//                .orElseThrow(() -> new EntityNotFoundException("User Assigned not founded"));

        Category category = categoryRepository.findById(incidentRequest.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        Incident incident = incidentMapper.toEntity(incidentRequest);
//        incident.setAssignedTo(assignedUser);
        incident.setCategory(category);

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }


    @Transactional(readOnly = true)
    public Page<IncidentResponse> getAllIncidents(IncidentFilterRequest filter, Pageable pageable) {
        Specification<Incident> specification = Specification.allOf(
                GenericSpec.equal("status", filter.status()),
                GenericSpec.equal("priority", filter.priority()),
                GenericSpec.like("title", filter.keyword()),
                GenericSpec.between(
                        "createdAt",
                        filter.startDate(),
                        filter.endDate()
                ),
                GenericSpec.equal("isDeleted", false)
        );

        UUID categoryId = parseUuid(filter.categoryId());
        if (categoryId != null) {
            specification = Specification.allOf(
                    specification,
                    GenericSpec.equalNested("category", "id", categoryId)
            );
        }
        return incidentRepository.findAll(specification, pageable).map(incidentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public IncidentResponse getDetailsIncidents(String id) {
        Incident incident = getActiveIncidentOrThrow(id);

        return incidentMapper.toResponse(incident);
    }

    /**
     * Edits the incident in place. Only title, description, category and priority are editable here.
     * Ticket number, status and assignee have their own rules (status transitions, the assign
     * endpoint), and a soft-deleted incident cannot be edited.
     */
    @Transactional
    public IncidentResponse updateIncident(String id, IncidentRequest incidentRequest) {
        Incident incident = getActiveIncidentOrThrow(id);

        Category category = categoryRepository.findById(incidentRequest.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        incident.setTitle(incidentRequest.title());
        incident.setDescription(incidentRequest.description());
        incident.setPriority(incidentRequest.priority());
        incident.setCategory(category);

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(String id, IncidentStatus incidentStatus) {
        Incident existing = getActiveIncidentOrThrow(id);
        validateIncidentStatusTransition(existing.getStatus(), incidentStatus);
        existing.setStatus(incidentStatus);

        return incidentMapper.toResponse(incidentRepository.save(existing));
    }

    @Transactional()
    public IncidentResponse assignIncident(String incidentId, UUID userId){
        Incident incident = getActiveIncidentOrThrow(incidentId);
        User assignedUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        incident.setAssignedTo(assignedUser);
        return incidentMapper.toResponse(incidentRepository.save(incident));

    }

    @Transactional
    public void softDeleteIncident(String id) {
        Incident incident = incidentRepository.getIncidentByIdAndIsDeleted(parseUuid(id),false).orElseThrow(() -> new EntityNotFoundException("Incident not found"));
        incident.markDeleted();
    }

    @Transactional
    public void forceDeleteIncident(String id) {
        Incident incident = incidentRepository.getIncidentByIdAndIsDeleted(parseUuid(id), false).orElseThrow(() -> new EntityNotFoundException("Incident not found"));
        incidentRepository.delete(incident);
    }


    private UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }
    private void validateIncidentStatusTransition(IncidentStatus currentStatus, IncidentStatus requestedStatus) {
        if (currentStatus.equals(requestedStatus)) {
            return;
        }
        if (currentStatus == IncidentStatus.CANCELED) {
            throw new ValidationException("Incident canceled");
        }
        Set<IncidentStatus> allowedStatuses = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedStatuses.contains(requestedStatus)) {
            throw new IllegalArgumentException("Incident status must transition from "
                    + currentStatus
                    + " to one of "
                    + allowedStatuses);
        }
    }

    private Incident getActiveIncidentOrThrow(String id){
        return incidentRepository.getIncidentByIdAndIsDeleted(parseUuid(id), false)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found or Deleted"));
    }
}
