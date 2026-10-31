package ch.bbw.pr.tresorbackend.service;

public interface PasswordEncryptService {
    String hashPassword(String password);

    boolean matchPassword(String password, String hashedPassword);
}
