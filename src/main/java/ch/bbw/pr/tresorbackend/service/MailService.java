package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.user.User;

public interface MailService {
    void sendPasswordResetHTML(String resetLink, String resetLinkExpirationTime, User user) throws ApplicationError;
}
