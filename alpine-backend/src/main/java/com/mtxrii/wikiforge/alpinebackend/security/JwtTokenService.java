package com.mtxrii.wikiforge.alpinebackend.security;

import java.time.Duration;
import java.time.Instant;

import com.mtxrii.wikiforge.alpinebackend.auth.dto.AuthResponse;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration accessTokenTtl;

    public JwtTokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.access-token-ttl}") Duration accessTokenTtl
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
    }

    public AuthResponse issueAuthResponse(UserResponse user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTtl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.id().toString())
                .claim("email", user.email())
                .claim("name", user.name())
                .claim("scope", "user")
                .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        return new AuthResponse(user, accessToken, "Bearer", accessTokenTtl.toSeconds());
    }
}
