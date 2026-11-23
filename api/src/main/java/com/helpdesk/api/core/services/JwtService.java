package com.helpdesk.api.core.services;

import com.helpdesk.api.accounts.entities.AppUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Signs and verifies access tokens. They are short-lived and carry the id of the server-side session
 * they belong to (claim {@value #SESSION_CLAIM}); whether that session is still live is checked per
 * request, which is what makes revocation immediate. Refresh tokens are not JWTs, see SessionService.
 */
@Service
public class JwtService {
    static final String SESSION_CLAIM = "sid";

    @Value("${application.security.jwt.secret}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public long getExpirationMillis(){
        return jwtExpiration;
    }

    public String generateToken(UserDetails userDetails, UUID sessionId){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        extraClaims.put(SESSION_CLAIM, sessionId.toString());
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration){
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifies the signature and expiry and returns the claims.
     *
     * @throws io.jsonwebtoken.JwtException if the token is malformed, tampered with or expired
     */
    public Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** @throws IllegalArgumentException if the token carries no (valid) session id, e.g. an old token */
    public UUID getSessionId(Claims claims){
        String sid = claims.get(SESSION_CLAIM, String.class);
        if (sid == null) {
            throw new IllegalArgumentException("Token has no session");
        }
        return UUID.fromString(sid);
    }

    public boolean isTokenValid(Claims claims, UserDetails userDetails){
        final String username = claims.getSubject();
        return username != null
                && username.equalsIgnoreCase(userDetails.getUsername())
                && claims.getExpiration() != null
                && !claims.getExpiration().before(new Date())
                && !issuedBeforePasswordChange(claims, userDetails);
    }

    /** A password change revokes every token issued before it. */
    private boolean issuedBeforePasswordChange(Claims claims, UserDetails userDetails) {
        if (!(userDetails instanceof AppUserDetails appUser) || appUser.getPasswordChangedAt() == null) {
            return false;
        }
        Date issuedAt = claims.getIssuedAt();
        return issuedAt == null || issuedAt.toInstant().isBefore(appUser.getPasswordChangedAt());
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
