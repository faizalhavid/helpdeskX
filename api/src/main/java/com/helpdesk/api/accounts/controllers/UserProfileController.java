package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.UserProfileRequest;
import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
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
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService profileService;

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> create(@PathVariable UUID userId,
                                                                    @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User profile created successfully",
                profileService.createProfile(userId, request)));
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("User profiles retrieved successfully",
                profileService.getAllProfiles(pageable)));
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> get(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved successfully",
                profileService.getProfileById(userId)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> update(@PathVariable UUID userId,
                                                                    @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User profile updated successfully",
                profileService.updateProfileById(userId, request)));
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable UUID userId) {
        profileService.deleteProfileById(userId);
        return ResponseEntity.ok(ApiResponse.ok("User profile deleted successfully", true));
    }
}
