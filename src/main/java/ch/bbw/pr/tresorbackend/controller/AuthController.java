package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final PasswordEncryptService passwordService;
    private final ReCAPTCHAService reCAPTCHAService;

    // build create User REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUser(@Valid @RequestBody RegisterUser registerUser, HttpServletResponse response) throws ApplicationError {
        TresorbackendApplication.getLogger().info("AuthController.createUser: RegisterUser(firstname = {}, lastname = {}, email = {})", registerUser.getFirstName(), registerUser.getLastName(), registerUser.getEmail());

        if (!registerUser.getPassword().equals(registerUser.getPasswordConfirmation())) {
            TresorbackendApplication.getLogger().info("AuthController.createUser: Passwords do not match");
            throw new ApplicationError(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        if (!reCAPTCHAService.verifyCaptcha(registerUser.getCaptchaToken())) {
            TresorbackendApplication.getLogger().info("AuthController.createUser: Captcha verification failed");
            throw new  ApplicationError(HttpStatus.BAD_REQUEST, "Captcha verification failed");
        }

        // transform registerUser to user
        User user = new User(
            null,
            registerUser.getFirstName(),
            registerUser.getLastName(),
            registerUser.getEmail(),
            passwordService.hashPassword(registerUser.getPassword()),
            BCrypt.gensalt(),
            Set.of(SecurityRole.USER)
        );

        User savedUser = userService.createUser(user);
        return ResponseEntity.accepted().body(authService.login(savedUser.getEmail(), response));
    }

    // simple login with no websecurity, just name and password
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> doLoginUser(@Valid @RequestBody LoginUser loginUser, HttpServletResponse response) throws ApplicationError {
        TresorbackendApplication.getLogger().info("AuthController.doLoginUser: LoginUser(email = {})", loginUser.getEmail());

        User user = userService.findByEmail(loginUser.getEmail());

        if (!passwordService.matchPassword(loginUser.getPassword(), user.getPassword())) {
            TresorbackendApplication.getLogger().info("AuthController.doLoginUser: provided password and hash didn't match");
            throw new ApplicationError(HttpStatus.BAD_REQUEST, "Email and password combination does not match");
        }

        TresorbackendApplication.getLogger().info("AuthController.doLoginUser: login successful");

        return ResponseEntity.ok(authService.login(loginUser.getEmail(), response));
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "__Host-refresh", required = false) String refreshToken, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/check")
    public ResponseEntity<Void> refresh() {
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "__Host-refresh", required = false) String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/forgottenpassword")
    public ResponseEntity<MessageResponse> handleUserForgottenPassword(@Valid @RequestBody EmailAdress emailAdress) {
        TresorbackendApplication.getLogger().info("UserController.handleUserForgottenPassword: EmailAddress(email = {})", emailAdress.getEmail());

        return ResponseEntity.ok(userService.handleUserForgottenPassword(emailAdress));
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/resetpassword")
    public ResponseEntity<MessageResponse> handleResetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        TresorbackendApplication.getLogger().info("UserController.handleResetPassword: ResetPassword(email = {}, resetToken = {})", resetPassword.getEmail(), resetPassword.getResetToken());

        return ResponseEntity.ok(userService.handleResetPassword(resetPassword));
    }
}
