package ch.bbw.pr.tresorbackend.twoFA;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.model.twoFA.TwoFactorAuthenticationEnableResponse;
import ch.bbw.pr.tresorbackend.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {
    private static final String ISSUER = "TresorAplikation";

    private final UserService userService;

    public TwoFactorAuthenticationEnableResponse enable2FA(String email) {
        User user = userService.findByEmail(email);
        if (user.is2FAEnabled()) {
            throw new ApplicationError(HttpStatus.BAD_REQUEST, "Two-Factor Authentication already enabled");
        }

        String secret = generateKey();
        String qrCodeUrl = generateQRUrl(secret, email);

        user.setTwoFASecret(secret);
        userService.updateUser(user);

        return new TwoFactorAuthenticationEnableResponse(secret, qrCodeUrl);
    }

    public void disable2FA(String email) {
        User user = userService.findByEmail(email);
        user.set2FAEnabled(false);
        user.setTwoFASecret(null);
        userService.updateUser(user);
    }

    @Override
    public boolean isValid(User user, String otp) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator(new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().build());
        if (!user.is2FAEnabled()) {
            if (gAuth.authorize(user.getTwoFASecret(), Integer.parseInt(otp))) {
                user.set2FAEnabled(true);
                userService.updateUser(user);
                return true;
            } else {
                throw new ApplicationError(HttpStatus.BAD_REQUEST, "OTP Code is invalid, 2FA hasn't been enabled. Please chach the code and try again!");
            }
        }

        return gAuth.authorize(user.getTwoFASecret(), Integer.parseInt(otp));
    }

    public boolean is2FAActive(User user) {
        return user.getTwoFASecret() != null && user.is2FAEnabled();
    }

    // Generate a new TOTP key
    private String generateKey() {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    // Generate a QR code URL for Google Authenticator
    private String generateQRUrl(String secret, String username) {
        String url = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            ISSUER,
            username,
            new GoogleAuthenticatorKey.Builder(secret).build());
        try {
            return generateQRBase64(url);
        } catch (Exception e) {
            return null;
        }
    }

    // Generate a QR code image in Base64 format
    private static String generateQRBase64(String qrCodeText) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
