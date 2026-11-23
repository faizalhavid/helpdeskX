package com.helpdesk.api.accounts.controllers;

import com.helpdesk.api.accounts.dto.requests.EmailRequest;
import com.helpdesk.api.accounts.dto.requests.LoginRequest;
import com.helpdesk.api.accounts.dto.requests.RefreshTokenRequest;
import com.helpdesk.api.accounts.dto.requests.RegisterRequest;
import com.helpdesk.api.accounts.dto.requests.ResetPasswordRequest;
import com.helpdesk.api.accounts.dto.requests.TokenRequest;
import com.helpdesk.api.accounts.dto.responses.AuthResponse;
import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.services.AuthService;
import com.helpdesk.api.core.configs.JwtAuthenticationDetails;
import com.helpdesk.api.core.models.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    /** Same response whether or not the email belongs to an account. */
    private static final String GENERIC_EMAIL_SENT =
            "If an account exists for that email, a message has been sent to it";

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    /** Public: the access token may already have expired. Authenticated by the refresh token itself. */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refresh(request)));
    }

    /** Revokes the session of the access token used for this request. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        authService.logout(JwtAuthenticationDetails.sessionIdOf(authentication));
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    /** Revokes every session of the current user, on every device. */
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal AppUserDetails principal) {
        authService.logoutAll(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Logged out of all sessions"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful. Check your email to verify your account"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody TokenRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully. You can now log in"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody EmailRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(ApiResponse.ok(GENERIC_EMAIL_SENT));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(GENERIC_EMAIL_SENT));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset. You can now log in"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.CurrentUser>> me(@AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("Current user retrieved successfully",
                AuthService.toCurrentUser(principal)));
    }
}
