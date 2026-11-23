package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.UserRequest;
import com.helpdesk.api.accounts.dto.responses.UserResponse;
import com.helpdesk.api.accounts.dto.requests.UserProfileRequest;
import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
import com.helpdesk.api.accounts.services.UserService;
import com.helpdesk.api.accounts.services.UserProfileService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserProfileService profileService;

    @PreAuthorize("hasAuthority('USER:CREATE')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", userService.createUser(request)));
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved successfully", userService.getAllUsers(pageable)));
    }

    @PreAuthorize("hasAuthority('USER:READ') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User retrieved successfully", userService.getUser(id)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
                                                              @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userService.updateUser(id, request)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> patch(@PathVariable UUID id,
                                                              @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userService.updateUser(id, request)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE') or #id == authentication.principal.id")
    @PostMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfile(@PathVariable UUID id,
                                                                            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User profile created successfully",
                profileService.createProfile(id, request)));
    }

    @PreAuthorize("hasAuthority('USER:READ') or #id == authentication.principal.id")
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully",
                profileService.getProfile(id)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE') or #id == authentication.principal.id")
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@PathVariable UUID id,
                                                                            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User profile updated successfully",
                profileService.updateProfile(id, request)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE') or #id == authentication.principal.id")
    @DeleteMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<Boolean>> deleteProfile(@PathVariable UUID id) {
        profileService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.ok("User profile deleted successfully", true));
    }

    /** Signs the user out of every device: their access and refresh tokens stop working at once. */
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PostMapping("/{id}/revoke-sessions")
    public ResponseEntity<ApiResponse<Integer>> revokeSessions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("User sessions revoked", userService.revokeSessions(id)));
    }

    @PreAuthorize("hasAuthority('USER:DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", true));
    }
}
