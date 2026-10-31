package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.EmailAdress;
import ch.bbw.pr.tresorbackend.model.ResetPassword;
import ch.bbw.pr.tresorbackend.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * UserService
 * @author Peter Rutschmann
 */
public interface UserService {
    User createUser(User user);

    User getUserById(Long userId);

    User findByEmail(String email);

    List<User> getAllUsers();

    User updateUser(User user);

    boolean deleteUser(Long userId);

    ResponseEntity<String> handleUserForgottenPassword(EmailAdress emailAdress);

    ResponseEntity<String> handleResetPassword(ResetPassword resetPassword);
}
