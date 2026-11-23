package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.dto.requests.UserRequest;
import com.helpdesk.api.accounts.dto.responses.UserProfileResponse;
import com.helpdesk.api.accounts.dto.responses.UserResponse;
import com.helpdesk.api.accounts.entities.Role;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserGroup;
import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import com.helpdesk.api.accounts.mappers.UserMapper;
import com.helpdesk.api.accounts.repositories.RoleRepository;
import com.helpdesk.api.accounts.repositories.UserGroupRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.exceptions.NoResourceFoundException;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder  passwordEncoder;
    private final SessionService sessionService;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = new User();
        // Accounts created by an administrator are trusted; self-registration goes through email verification.
        user.setEmailVerified(true);
        apply(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository.findAllByIsDeletedFalse(pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        return userMapper.toResponse(findActive(id));
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = findActive(id);
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), id)) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new IllegalArgumentException("Email is already in use");
        }
        apply(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findActive(id);
        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        sessionService.revokeAll(id, SessionRevocationReason.USER_DISABLED);
    }

    /** Signs the user out everywhere, e.g. after a suspected account compromise. @return sessions ended */
    @Transactional
    public int revokeSessions(UUID id) {
        findActive(id);
        return sessionService.revokeAll(id, SessionRevocationReason.ADMIN_REVOKED);
    }

    private void apply(User user, UserRequest request) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            if (user.getId() != null) {
                // Revoke access tokens issued before this change, and end every session.
                user.setPasswordChangedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
                sessionService.revokeAll(user.getId(), SessionRevocationReason.PASSWORD_CHANGED);
            }
        }
        if (request.roleId() != null) {
            user.setRole(roleRepository.findById(UUID.fromString(request.roleId()))
                    .filter(role -> !Boolean.TRUE.equals(role.getIsDeleted()))
                    .orElseThrow(() -> new NoResourceFoundException("Role not found")));
        } else if (user.getId() == null) {
            user.setRole(null);
        }
        if (request.userGroupId() != null) {
            user.setUserGroup(userGroupRepository.findById(UUID.fromString(request.userGroupId()))
                    .filter(group -> !Boolean.TRUE.equals(group.getIsDeleted()))
                    .orElseThrow(() -> new NoResourceFoundException("User group not found")));
        } else if (user.getId() == null) {
            user.setUserGroup(null);
        }
    }

    private User findActive(UUID id) {
        return userRepository.findById(id)
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .orElseThrow(() -> new NoResourceFoundException("User not found"));
    }

}
