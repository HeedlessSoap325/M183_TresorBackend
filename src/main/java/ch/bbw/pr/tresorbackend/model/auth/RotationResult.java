package ch.bbw.pr.tresorbackend.model.auth;

import ch.bbw.pr.tresorbackend.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RotationResult {
    private User user;

    private String refreshToken;
}
