package ch.bbw.pr.tresorbackend.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.UUID;

@Value
public class ResetPassword {
    @NotEmpty (message="E-Mail is required.")
    private String email;

    @NotEmpty (message="New password is required.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=*!])(?=\\S+$).{8,20}$",
            message = "Password requirements 8 to 20 characters, one uppercase, one lowercase, one digit, special sign: @#$%^&+=*!, no whitespace"
    )
    private String newPassword;

    @NotEmpty (message="New-Password-confirmation is required.")
    private String newPasswordConfirmation;

    @NotEmpty(message = "resetToken is required.")
    private UUID resetToken;
}
