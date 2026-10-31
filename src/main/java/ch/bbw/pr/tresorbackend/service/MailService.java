package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.User;

public interface MailService {
    void sendPasswordResetHTML(String resetLink, String resetLinkExpirationTime, User user);
}
