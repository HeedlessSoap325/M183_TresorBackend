package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.RotationResult;
import ch.bbw.pr.tresorbackend.model.User;

public interface RefreshTokenService {
    String generateToken();

    String hashToken(String token) throws ApplicationError;

    String createRefreshToken(User user) throws ApplicationError;

    RotationResult validateAndRotate(String rawToken) throws ApplicationError;

    void revokeToken(String rawToken) throws ApplicationError;
}
