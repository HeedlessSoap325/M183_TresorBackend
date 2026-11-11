package ch.bbw.pr.tresorbackend.util;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import org.springframework.http.HttpStatus;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * EncryptUtil
 * Used to encrypt content.
 * Not implemented yet.
 * @author Peter Rutschmann
 */
public class EncryptUtil {
    private static SecretKey secret;
    private final String algorithm = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public EncryptUtil(String secretKey, String salt) throws ApplicationError {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            TresorbackendApplication.getLogger().error(e.getMessage());
            throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize EncryptUtil");
        }
    }

    public String encrypt(String data) throws ApplicationError {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);

            // Generate a fresh random IV for every encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // init cipher and encrypt data
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secret, spec);
            byte[] cipherText = cipher.doFinal(data.getBytes());

            // Prepend IV to ciphertext so decrypt() can retrieve it later
            byte[] result = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            TresorbackendApplication.getLogger().error(e.getMessage());
            throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Encrypt failed");
        }
    }

    public String decrypt(String data) throws ApplicationError {
        try {
            byte[] decoded = Base64.getDecoder().decode(data);

            // Extract the IV we prepended during encryption
            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

            // init cipher and decrypt the data
            Cipher cipher = Cipher.getInstance(algorithm);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secret, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            TresorbackendApplication.getLogger().error(e.getMessage());
            throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Decrypt failed");
        }
    }
}
