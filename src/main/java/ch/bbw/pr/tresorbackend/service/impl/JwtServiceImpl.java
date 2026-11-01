package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.model.SecurityRole;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;
    private final ConfigProperties configProperties;

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        String scope = user.getAuthorities().stream()
            .map(SecurityRole::getValue)
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims =  JwtClaimsSet.builder()
            .issuer("tresorbackend")
            .issuedAt(now)
            .expiresAt(now.plusMillis(configProperties.getAccessTokenExpiration()))
            .subject(user.getEmail())
            .claim("roles", scope)
            .build();

        JwsHeader header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build();

        return jwtEncoder
            .encode(JwtEncoderParameters.from(
                header, claims
            )).getTokenValue();
    }
}
