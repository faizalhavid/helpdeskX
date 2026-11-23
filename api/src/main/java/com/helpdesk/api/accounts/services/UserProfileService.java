package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.dto.requests.UserProfileRequest;
import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserProfile;
import com.helpdesk.api.accounts.mappers.UserProfileMapper;
import com.helpdesk.api.accounts.repositories.UserProfileRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.exceptions.NoResourceFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper profileMapper;

    @Transactional
    public UserProfileResponse createProfile(UUID userId, UserProfileRequest request) {
        User user = findUser(userId);
        if (profileRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("User profile already exists");
        }
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(request.fullName());
        profile.setPicture(request.picture());
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return profileMapper.toResponse(profileRepository.findByUserId(userId)
                .filter(profile -> !Boolean.TRUE.equals(profile.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User profile not found")));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(UUID profileId) {
        return profileMapper.toResponse(findActiveProfile(profileId));
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getAllProfiles(Pageable pageable) {
        return profileRepository.findAllByIsDeletedFalse(pageable).map(profileMapper::toResponse);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UserProfileRequest request) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .filter(existing -> !Boolean.TRUE.equals(existing.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User profile not found"));
        profile.setFullName(request.fullName());
        profile.setPicture(request.picture());
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public UserProfileResponse updateProfileById(UUID profileId, UserProfileRequest request) {
        UserProfile profile = findActiveProfile(profileId);
        profile.setFullName(request.fullName());
        profile.setPicture(request.picture());
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .filter(existing -> !Boolean.TRUE.equals(existing.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User profile not found"));
        profile.markDeleted();
        profileRepository.save(profile);
    }

    @Transactional
    public void deleteProfileById(UUID profileId) {
        UserProfile profile = findActiveProfile(profileId);
        profile.markDeleted();
        profileRepository.save(profile);
    }

    private UserProfile findActiveProfile(UUID profileId) {
        return profileRepository.findById(profileId)
                .filter(profile -> !Boolean.TRUE.equals(profile.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User profile not found"));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User not found"));
    }
}
