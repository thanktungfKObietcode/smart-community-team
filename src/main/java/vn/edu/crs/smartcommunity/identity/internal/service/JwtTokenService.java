package vn.edu.crs.smartcommunity.identity.internal.service;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import vn.edu.crs.smartcommunity.identity.internal.config.JwtProperties;
import vn.edu.crs.smartcommunity.identity.internal.security.ApplicationUserDetails;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String createToken(ApplicationUserDetails user, Instant issuedAt) {
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.expirationSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("fullName", user.getFullName())
                .claim("roles", user.getRoles())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
