package com.helpdesk.api.accounts.services;

import com.helpdesk.api.accounts.configs.AuthProperties;
import com.helpdesk.api.accounts.dto.requests.EmailRequest;
import com.helpdesk.api.accounts.dto.requests.LoginRequest;
import com.helpdesk.api.accounts.dto.requests.RefreshTokenRequest;
import com.helpdesk.api.accounts.dto.requests.RegisterRequest;
import com.helpdesk.api.accounts.dto.requests.ResetPasswordRequest;
import com.helpdesk.api.accounts.dto.requests.TokenRequest;
import com.helpdesk.api.accounts.dto.responses.AuthResponse;
import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.entities.Role;
import com.helpdesk.api.accounts.entities.User;
import com.helpdesk.api.accounts.entities.UserProfile;
import com.helpdesk.api.accounts.entities.enums.SessionRevocationReason;
import com.helpdesk.api.accounts.entities.enums.TokenType;
import com.helpdesk.api.accounts.events.AuthEmailEvent;
import com.helpdesk.api.accounts.repositories.RoleRepository;
import com.helpdesk.api.accounts.repositories.UserRepository;
import com.helpdesk.api.core.exceptions.EmailNotVerifiedException;
import com.helpdesk.api.core.exceptions.InvalidRefreshTokenException;
import com.helpdesk.api.core.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService tokenService;
    private final SessionService sessionService;
    private final ApplicationEventPublisher events;
    private final AuthProperties authProperties;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username().trim(), request.password()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();
        // Checked only after the password matched, so it does not reveal anything to a guesser.
        if (!principal.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email address before logging in");
        }
        return toAuthResponse(principal, sessionService.startSession(principal.getId()));
    }

    /**
     * Exchanges a refresh token for a new access token and a new refresh token (rotation). The old
     * refresh token stops working; presenting it again revokes the whole session.
     *
     * <p>{@code noRollbackFor}: that revocation must be committed even though this call fails with 401.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResponse refresh(RefreshTokenRequest request) {
        SessionService.Rotated rotated = sessionService.rotate(request.refreshToken());
        return toAuthResponse(rotated.principal(), rotated.session());
    }

    /** Ends one session: its access token and refresh tokens stop working immediately. */
    public void logout(UUID sessionId) {
        sessionService.revoke(sessionId, SessionRevocationReason.LOGOUT);
    }

    /** Ends every session of the user, on every device. */
    public void logoutAll(UUID userId) {
        sessionService.revokeAll(userId, SessionRevocationReason.LOGOUT_ALL);
    }

    /** Creates an unverified account with the default role and emails a verification link. */
    @Transactional
    public void register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        Role defaultRole = roleRepository.findByNameIgnoreCaseAndIsDeletedFalse(authProperties.defaultRole())
                .orElseThrow(() -> new IllegalStateException(
                        "Default role '" + authProperties.defaultRole() + "' is not configured"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(defaultRole);
        user.setEmailVerified(false);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            UserProfile profile = new UserProfile();
            profile.setFullName(request.fullName().trim());
            profile.setUser(user);
            user.setProfile(profile);
        }
        userRepository.save(user);

        sendVerification(user);
    }

    @Transactional
    public void verifyEmail(TokenRequest request) {
        User user = tokenService.consume(request.token(), TokenType.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
    }

    /** Always succeeds from the caller's point of view, so it cannot be used to probe for accounts. */
    @Transactional
    public void resendVerification(EmailRequest request) {
        findByEmail(request.email())
                .filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(this::sendVerification);
    }

    /** Always succeeds from the caller's point of view, so it cannot be used to probe for accounts. */
    @Transactional
    public void forgotPassword(EmailRequest request) {
        findByEmail(request.email()).ifPresent(user -> {
            String rawToken = tokenService.issue(user, TokenType.PASSWORD_RESET);
            events.publishEvent(new AuthEmailEvent(AuthEmailEvent.Kind.PASSWORD_RESET,
                    user.getEmail(), user.getUsername(), rawToken));
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = tokenService.consume(request.token(), TokenType.PASSWORD_RESET);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // Tokens issued before this instant are rejected (JwtService#isTokenValid). JWT "iat" has
        // second precision, so the timestamp is truncated to match.
        user.setPasswordChangedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        // The reset link proved control of the mailbox, which is what verification checks.
        user.setEmailVerified(true);
        // Whoever knew the old password (or held a stolen session) is signed out everywhere.
        sessionService.revokeAll(user.getId(), SessionRevocationReason.PASSWORD_CHANGED);
        events.publishEvent(new AuthEmailEvent(AuthEmailEvent.Kind.PASSWORD_CHANGED,
                user.getEmail(), user.getUsername(), null));
    }

    private AuthResponse toAuthResponse(AppUserDetails principal, SessionService.IssuedSession session) {
        return new AuthResponse(
                jwtService.generateToken(principal, session.sessionId()),
                "Bearer",
                jwtService.getExpirationMillis() / 1000,
                session.refreshToken(),
                session.refreshExpiresInSeconds(),
                toCurrentUser(principal)
        );
    }

    private void sendVerification(User user) {
        String rawToken = tokenService.issue(user, TokenType.EMAIL_VERIFICATION);
        events.publishEvent(new AuthEmailEvent(AuthEmailEvent.Kind.EMAIL_VERIFICATION,
                user.getEmail(), user.getUsername(), rawToken));
    }

    private Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email.trim());
    }

    public static AuthResponse.CurrentUser toCurrentUser(AppUserDetails principal) {
        return new AuthResponse.CurrentUser(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRoleName(),
                principal.getPrivileges()
        );
    }
}
