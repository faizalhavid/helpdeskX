package com.helpdesk.api.core.configs;

import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.services.SessionService;
import com.helpdesk.api.core.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
        try {
            final Claims claims = jwtService.parseClaims(jwt);
            final String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Authorities are reloaded from the database on every request, so revoking a
                // privilege or deleting a user takes effect immediately instead of when the token expires.
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (userDetails instanceof AppUserDetails user
                        && user.isEnabled()
                        && user.isEmailVerified()
                        && jwtService.isTokenValid(claims, user)) {
                    // Logout, "log out everywhere", a password change and refresh-token reuse all end
                    // the session, so the access token stops working at once rather than at expiry.
                    UUID sessionId = jwtService.getSessionId(claims);
                    if (sessionService.isLive(sessionId, user.getId())) {
                        UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken.authenticated(
                                user,
                                null,
                                user.getAuthorities()
                        );
                        authToken.setDetails(new JwtAuthenticationDetails(request.getRemoteAddr(), sessionId));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            // Invalid/expired/unknown-user token: leave the context empty so the entry point answers 401.
            logger.debug("JWT authentication failed: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
