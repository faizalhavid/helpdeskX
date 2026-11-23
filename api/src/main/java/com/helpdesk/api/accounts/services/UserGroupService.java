package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.dto.requests.UserGroupRequest;
import com.helpdesk.api.accounts.dto.responses.UserGroupResponse;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserGroup;
import com.helpdesk.api.accounts.repositories.UserGroupRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.exceptions.NoResourceFoundException;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserGroupResponse createGroup(UserGroupRequest request) {
        if (groupRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("User group name is already in use");
        }
        if (request.email() != null && groupRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("User group email is already in use");
        }
        UserGroup group = new UserGroup();
        apply(group, request);
        return toResponse(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public Page<UserGroupResponse> getAllGroups(Pageable pageable) {
        return groupRepository.findAllByIsDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserGroupResponse getGroup(UUID id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public UserGroupResponse updateGroup(UUID id, UserGroupRequest request) {
        UserGroup group = findActive(id);
        if (groupRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("User group name is already in use");
        }
        if (request.email() != null && groupRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("User group email is already in use");
        }
        apply(group, request);
        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public void deleteGroup(UUID id) {
        UserGroup group = findActive(id);
        for (User user : userRepository.findAllByUserGroupId(id)) {
            user.setUserGroup(null);
        }
        group.markDeleted();
        groupRepository.save(group);
    }

    private void apply(UserGroup group, UserGroupRequest request) {
        group.setName(request.name());
                group.setDescription(request.description());
                group.setEmail(request.email());
    }

    private UserGroup findActive(UUID id) {
        return groupRepository.findById(id)
                .filter(group -> !Boolean.TRUE.equals(group.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User group not found"));
    }

    private UserGroupResponse toResponse(UserGroup group) {
        return new UserGroupResponse(AuditMetadataDTO.from(group), group.getName(), group.getDescription(),
                group.getEmail());
    }
}
