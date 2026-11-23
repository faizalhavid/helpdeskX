package com.helpdesk.api.accounts.mappers;

import com.helpdesk.api.accounts.dto.requests.UserRequest;
import com.helpdesk.api.accounts.dto.responses.UserResponse;
import com.helpdesk.api.accounts.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = UserProfileMapper.class
)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "incidents", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "userGroup", ignore = true)
    User toEntity(UserRequest userRequest);

    @Mapping(target = "metadata", expression = "java(com.helpdesk.api.core.models.AuditMetadataDTO.from(user))")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "userGroupId", source = "userGroup.id")
    UserResponse toResponse(User user);

}
