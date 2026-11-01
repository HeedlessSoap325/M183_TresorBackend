package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.model.RefreshToken;
import ch.bbw.pr.tresorbackend.model.RotationResult;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.RefreshTokenRepository;
import ch.bbw.pr.tresorbackend.service.JwtService;
import ch.bbw.pr.tresorbackend.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConfigProperties configProperties;
    private final JwtService jwtService;

    @Override
    public String generateToken() {
        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String createRefreshToken(User user) {
        String rawToken = generateToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiryDate(
            Instant.now().plusMillis(configProperties.getRefreshTokenExpiration())
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    @Transactional
    public RotationResult validateAndRotate(String rawToken) {
        String hash = hashToken(rawToken);

        RefreshToken oldToken = refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() ->
                new RuntimeException("Invalid refresh token")
            );

        if (oldToken.isRevoked()) {
            throw new RuntimeException("Token has been revoked");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token has expired");
        }

        User user = oldToken.getUser();

        // Invalidate previous refresh token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return new RotationResult(user, createRefreshToken(user));
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {

        String hash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
            .findByTokenHash(hash)
            .orElse(null);

        if (refreshToken != null) {
            refreshToken.setRevoked(true);
        }
    }
}
