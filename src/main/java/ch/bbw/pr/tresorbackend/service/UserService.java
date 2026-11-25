package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.user.EmailAdress;
import ch.bbw.pr.tresorbackend.model.MessageResponse;
import ch.bbw.pr.tresorbackend.model.auth.ResetPassword;
import ch.bbw.pr.tresorbackend.model.user.User;

import java.util.List;

/**
 * UserService
 * @author Peter Rutschmann
 */
public interface UserService {
    User createUser(User user) throws ApplicationError;

    User getUserById(Long userId) throws ApplicationError;

    User findByEmail(String email) throws ApplicationError;

    List<User> getAllUsers() throws ApplicationError;

    User updateUser(User user) throws ApplicationError;

    boolean deleteUser(Long userId);

    MessageResponse handleUserForgottenPassword(EmailAdress emailAdress) throws ApplicationError;

    MessageResponse handleResetPassword(ResetPassword resetPassword) throws  ApplicationError;
}
