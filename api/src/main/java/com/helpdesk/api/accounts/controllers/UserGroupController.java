package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.UserGroupRequest;
import com.helpdesk.api.accounts.dto.responses.UserGroupResponse;
import com.helpdesk.api.accounts.services.UserGroupService;
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
@RequestMapping("/api/user-groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserGroupService groupService;

    @PreAuthorize("hasAuthority('USER_GROUP:CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserGroupResponse>> create(@Valid @RequestBody UserGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User group created successfully", groupService.createGroup(request)));
    }

    @PreAuthorize("hasAuthority('USER_GROUP:READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserGroupResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("User groups retrieved successfully", groupService.getAllGroups(pageable)));
    }

    @PreAuthorize("hasAuthority('USER_GROUP:READ')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserGroupResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User group retrieved successfully", groupService.getGroup(id)));
    }

    @PreAuthorize("hasAuthority('USER_GROUP:UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserGroupResponse>> update(@PathVariable UUID id,
                                                                  @Valid @RequestBody UserGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User group updated successfully", groupService.updateGroup(id, request)));
    }

    @PreAuthorize("hasAuthority('USER_GROUP:DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.ok("User group deleted successfully", true));
    }
}
