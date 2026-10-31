package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * PasswordEncryptService
 *   used to hash password and verify match
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptServiceImpl implements PasswordEncryptService {
    private final ConfigProperties configProperties;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public PasswordEncryptServiceImpl(ConfigProperties configProperties) {
        this.configProperties = configProperties;
        bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public String hashPassword(String password) {
        return bCryptPasswordEncoder.encode(password + configProperties.getPepper());
    }

    @Override
    public boolean matchPassword(String password, String hashedPassword) {
        return bCryptPasswordEncoder.matches(password + configProperties.getPepper(), hashedPassword);
    }
}