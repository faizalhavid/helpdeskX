package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.dto.requests.PrivilegeRequest;
import com.helpdesk.api.accounts.dto.responses.PrivilegeResponse;
import com.helpdesk.api.accounts.entities.Privilege;
import com.helpdesk.api.accounts.entities.Role;
import com.helpdesk.api.accounts.repositories.PrivilegeRepository;
import com.helpdesk.api.accounts.repositories.RoleRepository;
import com.helpdesk.api.core.exceptions.NoResourceFoundException;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivilegeService {
    private final PrivilegeRepository privilegeRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public PrivilegeResponse createPrivilege(PrivilegeRequest request) {
        String resource = normalizeResource(request.resource());
        if (privilegeRepository.existsByResourceIgnoreCaseAndAction(resource, request.action())) {
            throw new IllegalArgumentException("Privilege " + resource + ":" + request.action() + " already exists");
        }
        Privilege privilege = new Privilege();
        apply(privilege, resource, request);
        return toResponse(privilegeRepository.save(privilege));
    }

    @Transactional(readOnly = true)
    public Page<PrivilegeResponse> getAllPrivileges(Pageable pageable) {
        return privilegeRepository.findAllByIsDeletedFalse(pageable).map(PrivilegeService::toResponse);
    }

    @Transactional(readOnly = true)
    public PrivilegeResponse getPrivilege(UUID id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public PrivilegeResponse updatePrivilege(UUID id, PrivilegeRequest request) {
        Privilege privilege = findActive(id);
        String resource = normalizeResource(request.resource());
        if (privilegeRepository.existsByResourceIgnoreCaseAndActionAndIdNot(resource, request.action(), id)) {
            throw new IllegalArgumentException("Privilege " + resource + ":" + request.action() + " already exists");
        }
        apply(privilege, resource, request);
        return toResponse(privilegeRepository.save(privilege));
    }

    @Transactional
    public void deletePrivilege(UUID id) {
        Privilege privilege = findActive(id);
        for (Role role : roleRepository.findAllByPrivilegesId(id)) {
            role.getPrivileges().remove(privilege);
        }
        privilege.setIsDeleted(true);
        privilege.setDeletedAt(LocalDateTime.now());
        privilegeRepository.save(privilege);
    }

    private void apply(Privilege privilege, String resource, PrivilegeRequest request) {
        privilege.setResource(resource);
        privilege.setAction(request.action());
        privilege.setDescription(request.description());
    }

    /** One canonical form, so the (resource, action) unique constraint also blocks "user" vs "USER". */
    private static String normalizeResource(String resource) {
        return resource.trim().toUpperCase(Locale.ROOT);
    }

    private Privilege findActive(UUID id) {
        return privilegeRepository.findById(id)
                .filter(privilege -> !Boolean.TRUE.equals(privilege.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("Privilege not found"));
    }

    static PrivilegeResponse toResponse(Privilege privilege) {
        return new PrivilegeResponse(
                AuditMetadataDTO.from(privilege),
                privilege.getResource(),
                privilege.getAction(),
                privilege.toAuthority(),
                privilege.getDescription()
        );
    }
}
