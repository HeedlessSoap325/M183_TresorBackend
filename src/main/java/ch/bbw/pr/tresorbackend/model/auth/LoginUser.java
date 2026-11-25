package ch.bbw.pr.tresorbackend.model.auth;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

/**
 * LoginUser
 *   DTO for simple login
 * @author Peter Rutschmann
 */
@Value
public class LoginUser {
    @NotEmpty (message="E-Mail is required.")
    private String email;

    @NotEmpty (message="Password is required.")
    private String password;

    @Nullable
    private String otp;
}