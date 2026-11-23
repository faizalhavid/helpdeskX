package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.dto.requests.RoleRequest;
import com.helpdesk.api.accounts.dto.responses.PrivilegeResponse;
import com.helpdesk.api.accounts.dto.responses.RoleResponse;
import com.helpdesk.api.accounts.entities.Privilege;
import com.helpdesk.api.accounts.entities.Role;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.repositories.PrivilegeRepository;
import com.helpdesk.api.accounts.repositories.RoleRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.exceptions.NoResourceFoundException;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.role())) {
            throw new IllegalArgumentException("Role is already in use");
        }
        Role role = new Role();
        role.setName(request.role());
        role.setDescription(request.description());
        role.setPrivileges(resolvePrivileges(request.privilegeIds()));
        return toResponse(roleRepository.save(role));
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAllByIsDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public RoleResponse updateRole(UUID id, RoleRequest request) {
        Role role = findActive(id);
        if (roleRepository.existsByNameIgnoreCaseAndIdNot(request.role(), id)) {
            throw new IllegalArgumentException("Role is already in use");
        }
        role.setName(request.role());
        role.setDescription(request.description());
        if (request.privilegeIds() != null) {
            role.setPrivileges(resolvePrivileges(request.privilegeIds()));
        }
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = findActive(id);
        for (User user : userRepository.findAllByRoleId(id)) {
            user.setRole(null);
        }
        role.getPrivileges().clear();
        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        roleRepository.save(role);
    }

    private Set<Privilege> resolvePrivileges(Set<UUID> privilegeIds) {
        if (privilegeIds == null || privilegeIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Privilege> found = privilegeRepository.findAllById(privilegeIds).stream()
                .filter(privilege -> !Boolean.TRUE.equals(privilege.getIsDeleted()))
                .toList();
        if (found.size() != privilegeIds.size()) {
            throw new NoResourceFoundException("One or more privileges were not found");
        }
        return new HashSet<>(found);
    }

    private Role findActive(UUID id) {
        return roleRepository.findById(id)
                .filter(role -> !Boolean.TRUE.equals(role.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("Role not found"));
    }

    private RoleResponse toResponse(Role role) {
        List<PrivilegeResponse> privileges = role.getPrivileges().stream()
                .filter(privilege -> !Boolean.TRUE.equals(privilege.getIsDeleted()))
                .sorted(Comparator.comparing(Privilege::toAuthority))
                .map(PrivilegeService::toResponse)
                .toList();
        return new RoleResponse(AuditMetadataDTO.from(role), role.getName(), role.getDescription(), privileges);
    }
}
