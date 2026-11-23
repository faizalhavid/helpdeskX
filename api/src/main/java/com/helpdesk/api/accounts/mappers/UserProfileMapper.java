package com.helpdesk.api.accounts.mappers;

import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
import com.helpdesk.api.accounts.entities.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "metadata", expression = "java(com.helpdesk.api.core.models.AuditMetadataDTO.from(profile))")
    @Mapping(target = "userId", source = "user.id")
    UserProfileResponse toResponse(UserProfile profile);


}
