package com.project.flow.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.project.flow.common.exception.InternalServerError;
import com.project.flow.common.exception.TokenExpiredException;
import com.project.flow.common.exception.UnauthorizedException;
import com.project.flow.config.JwtConfig;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtConfig jwtConfig;

    public JwtService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generateAccessToken(UUID userId, String email, String status) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(userId.toString())
            .claim("userId", userId.toString())
            .claim("email", email)
            .claim("status", status)
            .issueTime(now)
            .expirationTime(expiration)
            .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        try {
            signedJWT.sign(new MACSigner(jwtConfig.getSecret().getBytes()));
            return signedJWT.serialize();
        } catch (JOSEException | IllegalStateException e) {
            throw new InternalServerError("Failed to generate access token", Optional.empty());
        }
    }

    public UUID extractUserId(String token) {
        JWTClaimsSet claims = extractValidClaims(token);
        String userId = extractStringClaim(claims, "userId");
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid token", Optional.empty());
        }
    }

    public String extractEmail(String token) {
        JWTClaimsSet claims = extractValidClaims(token);
        return extractStringClaim(claims, "email");
    }

    public String extractStatus(String token) {
        JWTClaimsSet claims = extractValidClaims(token);
        return extractStringClaim(claims, "status");
    }

    public boolean isTokenValid(String token) {
        try {
            SignedJWT signedJWT = parseSignedJwt(token);
            verifySignatureOrThrow(signedJWT);
            ensureNotExpired(claimsOrThrow(signedJWT));
            return true;
        } catch (TokenExpiredException | UnauthorizedException e) {
            return false;
        }
    }

    private JWTClaimsSet extractValidClaims(String token) {
        SignedJWT signedJWT = parseSignedJwt(token);
        verifySignatureOrThrow(signedJWT);

        JWTClaimsSet claimsSet;
        try {
            claimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new UnauthorizedException("Invalid token", e, Optional.empty());
        }

        ensureNotExpired(claimsSet);
        return claimsSet;
    }

    private SignedJWT parseSignedJwt(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new UnauthorizedException("Invalid token", e, Optional.empty());
        }
    }

    private JWTClaimsSet claimsOrThrow(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new UnauthorizedException("Invalid token", e, Optional.empty());
        }
    }

    private void verifySignatureOrThrow(SignedJWT signedJWT) {
        try {
            MACVerifier verifier = new MACVerifier(jwtConfig.getSecret().getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new UnauthorizedException("Invalid token", Optional.empty());
            }
        } catch (JOSEException | IllegalArgumentException e) {
            throw new InternalServerError("JWT verification failed", Optional.empty());
        }
    }

    private void ensureNotExpired(JWTClaimsSet claimsSet) {
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime != null && expirationTime.before(new Date())) {
            throw new TokenExpiredException("Token expired", Optional.empty());
        }
    }

    private String extractStringClaim(JWTClaimsSet claimsSet, String claimName) {
        Object value = claimsSet.getClaim(claimName);
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }

        throw new UnauthorizedException("Invalid token", Optional.empty());
    }
}
