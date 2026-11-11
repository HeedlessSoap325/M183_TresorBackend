package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse login(String email, HttpServletResponse response) throws ApplicationError;

    AuthResponse refreshToken(String refreshToken, HttpServletResponse response) throws ApplicationError;

    void logout(String refreshToken, HttpServletResponse response) throws ApplicationError;
}
