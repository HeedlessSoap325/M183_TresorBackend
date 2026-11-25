package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.user.User;

public interface JwtService {
    String generateAccessToken(User user);
}
