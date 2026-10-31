package ch.bbw.pr.tresorbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "forgotten_password_token")
public class ForgottenPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Date issue;

    @Column(nullable = false)
    private String email;

    public ForgottenPasswordToken(String email) {
        this.issue = new Date();
        this.email = email;
    }

    public ForgottenPasswordToken() {
        this.issue = null;
        this.email = null;
    }

    public boolean isExpired() {
        return this.issue != null && this.issue.before(new Date(System.currentTimeMillis() - 10 * 60 * 1000)); // 10 Minutes
    }
}
