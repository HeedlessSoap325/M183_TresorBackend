package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // build get user by id REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok().body(user);
    }

    // Build Get All Users REST API
    // http://localhost:8080/api/users
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok().body(users);
    }

    // Build Update User REST API
    // http://localhost:8080/api/users/1
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PutMapping("{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long userId, @Valid @RequestBody User user) {
        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok().body(updatedUser);
    }

    // Build Delete User REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) {
        if( userService.deleteUser(userId))
            return ResponseEntity.ok().body("User successfully deleted!");
        return ResponseEntity.notFound().build();
    }

    // get user id by email
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
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
}
