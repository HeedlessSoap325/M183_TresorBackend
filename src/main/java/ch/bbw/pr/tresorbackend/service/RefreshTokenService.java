package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.RotationResult;
import ch.bbw.pr.tresorbackend.model.User;

public interface RefreshTokenService {
    String generateToken();

    String hashToken(String token);

    String createRefreshToken(User user);

    RotationResult validateAndRotate(String rawToken);

    void revokeToken(String rawToken);
}
