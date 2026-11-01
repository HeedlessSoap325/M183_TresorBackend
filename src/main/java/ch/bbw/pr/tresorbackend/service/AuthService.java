package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    AuthResponse login(String email, HttpServletResponse response);

    ResponseEntity<AuthResponse> refreshToken(String refreshToken, HttpServletResponse response);

    void logout(String refreshToken, HttpServletResponse response);
}
