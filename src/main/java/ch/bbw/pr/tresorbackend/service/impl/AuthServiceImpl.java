package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.AuthService;
import ch.bbw.pr.tresorbackend.service.JwtService;
import ch.bbw.pr.tresorbackend.service.RefreshTokenService;
import ch.bbw.pr.tresorbackend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final ConfigProperties configProperties;

    @Override
    public AuthResponse login(String email, HttpServletResponse response) {
        User user = userService.findByEmail(email);

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = refreshTokenService.createRefreshToken(user);
        addRefreshTokenCookie(response, refreshToken);

        return new AuthResponse(
            accessToken,
            "Bearer "
        );
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            deleteRefreshTokenCookie(response);
            return ResponseEntity.badRequest().body(new AuthResponse());
        }

        RotationResult result = refreshTokenService.validateAndRotate(refreshToken);
        String accessToken = jwtService.generateAccessToken(result.getUser());
        addRefreshTokenCookie(response, result.getRefreshToken());

        return ResponseEntity.ok().body(
            new AuthResponse(
                accessToken,
                "Bearer "
            )
        );
    }

    @Override
    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        deleteRefreshTokenCookie(response);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie
            .from("__Host-refresh", refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(configProperties.getRefreshTokenExpiration())
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
            .from("__Host-refresh", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();

        response.addHeader(
            HttpHeaders.SET_COOKIE,
            cookie.toString()
        );
    }
}
