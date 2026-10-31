package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.impl.PasswordEncryptServiceImpl;
import ch.bbw.pr.tresorbackend.service.ReCAPTCHAService;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncryptServiceImpl passwordService;
    private final ReCAPTCHAService reCAPTCHAService;

    // build create User REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody RegisterUser registerUser) {
        TresorbackendApplication.getLogger().info("UserController.createUser: RegisterUser(firstname = {}, lastname = {}, email = {})", registerUser.getFirstName(), registerUser.getLastName(), registerUser.getEmail());

        if (!registerUser.getPassword().equals(registerUser.getPasswordConfirmation())) {
            TresorbackendApplication.getLogger().info("UserController.createUser: Passwords do not match");
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        if (!reCAPTCHAService.verifyCaptcha(registerUser.getCaptchaToken())) {
            TresorbackendApplication.getLogger().info("UserController.createUser: Captcha verification failed");
            return ResponseEntity.badRequest().body("Captcha verification failed");
        }

        // transform registerUser to user
        User user = new User(
                null,
                registerUser.getFirstName(),
                registerUser.getLastName(),
                registerUser.getEmail(),
                passwordService.hashPassword(registerUser.getPassword()),
                BCrypt.gensalt()
        );

        User savedUser = userService.createUser(user);
        JsonObject obj = new JsonObject();
        if (savedUser != null) {
            TresorbackendApplication.getLogger().info("UserController.createUser: user saved in db");
            obj.addProperty("answer", "User saved");
        } else {
            TresorbackendApplication.getLogger().info("UserController.createUser: user not saved in db");
            obj.addProperty("answer", "User not saved");
        }

        String json = new Gson().toJson(obj);
        return ResponseEntity.accepted().body(json);
    }

    // build get user by id REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(user);
    }

    // Build Get All Users REST API
    // http://localhost:8080/api/users
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(users);
    }

    // Build Update User REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PutMapping("{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long userId, @Valid @RequestBody User user) {
        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        if (updatedUser == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(updatedUser);
    }

    // Build Delete User REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) {
        if( userService.deleteUser(userId))
            return ResponseEntity.ok().body("User successfully deleted!");
        return ResponseEntity.notFound().build();
    }

    // get user id by email
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PostMapping("/byemail")
    public ResponseEntity<String> getUserIdByEmail(@Valid @RequestBody EmailAdress email) {
        TresorbackendApplication.getLogger().info("UserController.getUserIdByEmail: EmailAddress(email = {})", email.getEmail());

        User user = userService.findByEmail(email.getEmail());
        if (user == null) {
            TresorbackendApplication.getLogger().info("UserController.getUserIdByEmail: no user found with email");
            JsonObject obj = new JsonObject();
            obj.addProperty("message", "No user found with this email");
            String json = new Gson().toJson(obj);

            return ResponseEntity.badRequest().body(json);
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("answer", user.getId());
        String json = new Gson().toJson(obj);

        return ResponseEntity.accepted().body(json);
    }

    // simple login with no websecurity, just name and password
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> doLoginUser(@Valid @RequestBody LoginUser loginUser) {
        TresorbackendApplication.getLogger().info("UserController.doLoginUser: LoginUser(email = {})", loginUser.getEmail());

        User user = userService.findByEmail(loginUser.getEmail());
        if (user == null) {
            TresorbackendApplication.getLogger().info("UserController.doLoginUser: user not found");
            return ResponseEntity.badRequest().body(new LoginResponse("No user found with this email", null));
        }

        if (!passwordService.matchPassword(loginUser.getPassword(), user.getPassword())) {
            TresorbackendApplication.getLogger().info("UserController.doLoginUser: provided password and hash didn't match");
            return ResponseEntity.badRequest().body(new LoginResponse("Username and Password don't match", null));
        }

        TresorbackendApplication.getLogger().info("UserController.doLoginUser: login successful");
        return ResponseEntity.ok(new LoginResponse("Login successful", user.getId()));
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PostMapping("/forgottenpassword")
    public ResponseEntity<String> handleUserForgottenPassword(@Valid @RequestBody EmailAdress emailAdress) {
        TresorbackendApplication.getLogger().info("UserController.handleUserForgottenPassword: EmailAddress(email = {})", emailAdress.getEmail());

        return userService.handleUserForgottenPassword(emailAdress);
    }

    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.crossOrigin}")
    @PostMapping("/resetpassword")
    public ResponseEntity<String> handleResetPassword(@Valid @RequestBody ResetPassword resetPassword) {
        TresorbackendApplication.getLogger().info("UserController.handleResetPassword: ResetPassword(email = {}, resetToken = {})", resetPassword.getEmail(), resetPassword.getResetToken());

        return userService.handleResetPassword(resetPassword);
    }
}
