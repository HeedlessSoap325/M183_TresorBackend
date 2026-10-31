package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.model.EmailAdress;
import ch.bbw.pr.tresorbackend.model.ForgottenPasswordToken;
import ch.bbw.pr.tresorbackend.model.ResetPassword;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.ForgottenPasswordTokenRepository;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.MailService;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptService;
import ch.bbw.pr.tresorbackend.service.SafeDbCall;
import ch.bbw.pr.tresorbackend.service.UserService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * UserServiceImpl
 * @author Peter Rutschmann
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private ForgottenPasswordTokenRepository forgottenPasswordTokenRepository;
    private PasswordEncryptService passwordEncryptService;
    private MailService mailService;

    @Override
    public User createUser(User user) {
        return SafeDbCall.safeDbCall(() -> userRepository.save(user), null);
    }

    @Override
    public User getUserById(Long userId) {
        Optional<User> user = SafeDbCall.safeDbCall(() -> userRepository.findById(userId), Optional.empty());
        return user.orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        Optional<User> user = SafeDbCall.safeDbCall(() -> userRepository.findByEmail(email), Optional.empty());
        return user.orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return (List<User>) SafeDbCall.safeDbCall(() -> userRepository.findAll(), List.of());
    }

    @Override
    public User updateUser(User user) {
        Optional<User> optionalExistingUser = SafeDbCall.safeDbCall(() ->
                        userRepository.findById(user.getId()),
                Optional.empty()
        );
        if (optionalExistingUser.isEmpty()) return null;

        User existingUser = optionalExistingUser.get();
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        User updatedUser = userRepository.save(existingUser);
        return updatedUser;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return SafeDbCall.safeDbCall(() -> userRepository.deleteById(userId));
    }

    @Override
    public ResponseEntity<String> handleUserForgottenPassword(EmailAdress emailAdress) {
        Optional<User> optionalExistingUser = SafeDbCall.safeDbCall(() ->
                        userRepository.findByEmail(emailAdress.getEmail()),
                Optional.empty()
        );

        if (optionalExistingUser.isEmpty()) return ResponseEntity.notFound().build();
        User user = optionalExistingUser.get();

        ForgottenPasswordToken token = forgottenPasswordTokenRepository.save(new ForgottenPasswordToken(emailAdress.getEmail()));

        String reset_link = "http://localhost:3000/user/reset_password?reset_token=" + token.getId();
        mailService.sendPasswordResetHTML(reset_link, "10 minutes", user);

        TresorbackendApplication.getLogger().info("UserServiceImpl.handleUserForgottenPassword: token.id = {}", token.getId());
        TresorbackendApplication.getLogger().info("http://localhost:3000/user/reset_password?reset_token={}", token.getId());

        JsonObject obj = new JsonObject();
        obj.addProperty("message", "A reset link has been sent to your email. The link will expire in 10 Minutes.");
        String json = new Gson().toJson(obj);
        return ResponseEntity.ok().body(json);
    }

    @Override
    public ResponseEntity<String> handleResetPassword(ResetPassword resetPassword) {
        Optional<ForgottenPasswordToken> forgottenPasswordTokenOptional = forgottenPasswordTokenRepository.findById(resetPassword.getResetToken());
        JsonObject obj = new JsonObject();

        if (
                forgottenPasswordTokenOptional.isEmpty() ||
                !forgottenPasswordTokenOptional.get().getId().equals(resetPassword.getResetToken()) ||
                !forgottenPasswordTokenOptional.get().getEmail().equals(resetPassword.getEmail())
        ) {
            obj.addProperty("message", "Reset link is invalid. Please check the link and provided email.");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }
        ForgottenPasswordToken token = forgottenPasswordTokenOptional.get();

        if (token.isExpired()) {
            TresorbackendApplication.getLogger().info("UserServiceImpl.handleResetPassword: request invalid ResetPassword(email = {}, resetToken = {})",  resetPassword.getEmail(), resetPassword.getResetToken());
            obj.addProperty("message", "Reset link expired. Please try again with a new reset link.");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }

        if (!resetPassword.getNewPassword().equals(resetPassword.getNewPasswordConfirmation())) {
            TresorbackendApplication.getLogger().info("UserServiceImpl.handleResetPassword: passwords didn't match.");
            obj.addProperty("message", "Passwords do not match.");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }

        Optional<User> optionalExistingUser = SafeDbCall.safeDbCall(() ->
                        userRepository.findByEmail(resetPassword.getEmail()),
                Optional.empty()
        );

        if (optionalExistingUser.isEmpty()) {
            obj.addProperty("message", "User not found.");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }

        User user = optionalExistingUser.get();
        user.setPassword(passwordEncryptService.hashPassword(resetPassword.getNewPassword()));

        userRepository.save(user);
        forgottenPasswordTokenRepository.delete(token);

        obj.addProperty("message", "Password changed successfully.");
        String json = new Gson().toJson(obj);
        return ResponseEntity.ok().body(json);
    }
}
