package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.PrivilegeRequest;
import com.helpdesk.api.accounts.dto.responses.PrivilegeResponse;
import com.helpdesk.api.accounts.services.PrivilegeService;
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
@RequestMapping("/api/privileges")
@RequiredArgsConstructor
public class PrivilegeController {
    private final PrivilegeService privilegeService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRIVILEGE:CREATE')")
    public ResponseEntity<ApiResponse<PrivilegeResponse>> create(@Valid @RequestBody PrivilegeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Privilege created successfully",
                privilegeService.createPrivilege(request)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRIVILEGE:READ')")
    public ResponseEntity<ApiResponse<Page<PrivilegeResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Privileges retrieved successfully",
                privilegeService.getAllPrivileges(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE:READ')")
    public ResponseEntity<ApiResponse<PrivilegeResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Privilege retrieved successfully",
                privilegeService.getPrivilege(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE:UPDATE')")
    public ResponseEntity<ApiResponse<PrivilegeResponse>> update(@PathVariable UUID id,
                                                                  @Valid @RequestBody PrivilegeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Privilege updated successfully",
                privilegeService.updatePrivilege(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRIVILEGE:DELETE')")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable UUID id) {
        privilegeService.deletePrivilege(id);
        return ResponseEntity.ok(ApiResponse.ok("Privilege deleted successfully", true));
    }
}
