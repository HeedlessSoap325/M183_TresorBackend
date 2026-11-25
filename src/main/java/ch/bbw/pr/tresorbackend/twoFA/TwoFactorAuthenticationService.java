package ch.bbw.pr.tresorbackend.twoFA;

import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.model.twoFA.TwoFactorAuthenticationEnableResponse;

public interface TwoFactorAuthenticationService {
    public TwoFactorAuthenticationEnableResponse enable2FA(String email);

    public void disable2FA(String email);

    public boolean isValid(User user, String otp);

    public boolean is2FAActive(User user);
}
