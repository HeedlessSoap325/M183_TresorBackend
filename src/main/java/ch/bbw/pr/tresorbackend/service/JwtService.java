package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.User;

public interface JwtService {
    String generateAccessToken(User user);
}
