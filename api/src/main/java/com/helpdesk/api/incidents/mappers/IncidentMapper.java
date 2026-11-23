package com.helpdesk.api.incidents.mappers;

import com.helpdesk.api.incidents.dto.requests.IncidentRequest;
import com.helpdesk.api.incidents.dto.responses.CategoryResponse;
import com.helpdesk.api.incidents.dto.responses.IncidentResponse;
import com.helpdesk.api.incidents.entities.Category;
import com.helpdesk.api.incidents.entities.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = com.helpdesk.api.accounts.mappers.UserProfileMapper.class
)
public interface IncidentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "status", ignore = true)
    Incident toEntity(IncidentRequest request);

    @Mapping(target = "metadata", expression = "java(com.helpdesk.api.core.models.AuditMetadataDTO.from(incident))")
    @Mapping(target = "assignedTo", source = "assignedTo.profile")
    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "metadata", expression = "java(com.helpdesk.api.core.models.AuditMetadataDTO.from(category))")
    CategoryResponse toCategoryResponse(Category category);
}
