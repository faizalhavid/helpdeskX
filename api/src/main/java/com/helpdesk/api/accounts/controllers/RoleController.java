package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.RoleRequest;
import com.helpdesk.api.accounts.dto.responses.RoleResponse;
import com.helpdesk.api.accounts.services.RoleService;
import com.helpdesk.api.core.models.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Role created successfully", roleService.createRole(request)));
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Roles retrieved successfully", roleService.getAllRoles(pageable)));
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Role retrieved successfully", roleService.getRole(id)));
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable UUID id,
                                                              @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Role updated successfully", roleService.updateRole(id, request)));
    }

    @PreAuthorize("hasAuthority('ROLE:DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.ok("Role deleted successfully", true));
    }
}
