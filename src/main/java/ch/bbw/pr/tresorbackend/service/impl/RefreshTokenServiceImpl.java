package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.model.RefreshToken;
import ch.bbw.pr.tresorbackend.model.RotationResult;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.RefreshTokenRepository;
import ch.bbw.pr.tresorbackend.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Override
    public String generateToken() {
        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    @Override
    public String hashToken(String token) throws ApplicationError {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            TresorbackendApplication.getLogger().error(e.getMessage());
            throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to hash refresh token");
        }
    }

    @Override
    public String createRefreshToken(User user) throws ApplicationError {
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
    public RotationResult validateAndRotate(String rawToken) throws ApplicationError {
        String hash = hashToken(rawToken);

        RefreshToken oldToken = refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() ->
                new ApplicationError(HttpStatus.BAD_REQUEST, "Invalid refresh token")
            );

        if (oldToken.isRevoked()) {
            throw new ApplicationError(HttpStatus.BAD_REQUEST, "Token has been revoked");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new ApplicationError(HttpStatus.BAD_REQUEST, "Token has expired");
        }

        User user = oldToken.getUser();

        // Invalidate previous refresh token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return new RotationResult(user, createRefreshToken(user));
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) throws ApplicationError {
        String hash = hashToken(rawToken);

        refreshTokenRepository
            .findByTokenHash(hash)
            .ifPresent(refreshToken -> refreshToken.setRevoked(true));
    }
}
