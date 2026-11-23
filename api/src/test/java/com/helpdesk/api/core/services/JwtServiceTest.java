package com.helpdesk.api.core.services;

import com.helpdesk.api.accounts.entities.AppUserDetails;
import com.helpdesk.api.accounts.entities.Role;
import com.helpdesk.api.accounts.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    // 256-bit key, base64: the format application.security.jwt.secret expects
    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    JwtService jwtService;
    User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        Role role = new Role();
        role.setName("AGENT");
        role.setPrivileges(new HashSet<>());
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("jane");
        user.setEmail("jane@x.com");
        user.setEmailVerified(true);
        user.setRole(role);
    }

    @Test
    void accessTokenCarriesTheSessionIdAndTheUser() {
        UUID sessionId = UUID.randomUUID();
        AppUserDetails principal = AppUserDetails.from(user);

        Claims claims = jwtService.parseClaims(jwtService.generateToken(principal, sessionId));

        assertThat(jwtService.getSessionId(claims)).isEqualTo(sessionId);
        assertThat(claims.getSubject()).isEqualTo("jane");
        assertThat(jwtService.isTokenValid(claims, principal)).isTrue();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(AppUserDetails.from(user), UUID.randomUUID());
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThatThrownBy(() -> jwtService.parseClaims(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String foreign = Jwts.builder().subject("jane")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                        "dGhpcy1pcy1hLWRpZmZlcmVudC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2g=")), Jwts.SIG.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.parseClaims(foreign)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String token = jwtService.generateToken(AppUserDetails.from(user), UUID.randomUUID());

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenWithoutASessionIsRejected() {
        // What a token issued before sessions existed looks like: validly signed, but no "sid".
        String legacy = Jwts.builder().subject("jane")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)), Jwts.SIG.HS256)
                .compact();
        Claims claims = jwtService.parseClaims(legacy);

        assertThatThrownBy(() -> jwtService.getSessionId(claims)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tokenIssuedBeforeAPasswordChangeIsInvalid() {
        String token = jwtService.generateToken(AppUserDetails.from(user), UUID.randomUUID());
        Claims claims = jwtService.parseClaims(token);

        user.setPasswordChangedAt(LocalDateTime.now().plusHours(1));

        assertThat(jwtService.isTokenValid(claims, AppUserDetails.from(user))).isFalse();
    }

    @Test
    void tokenForAnotherUserIsInvalid() {
        Claims claims = jwtService.parseClaims(jwtService.generateToken(AppUserDetails.from(user), UUID.randomUUID()));
        user.setUsername("someone-else");

        assertThat(jwtService.isTokenValid(claims, AppUserDetails.from(user))).isFalse();
    }
}
