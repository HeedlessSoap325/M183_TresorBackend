package ch.bbw.pr.tresorbackend.repository;

import ch.bbw.pr.tresorbackend.model.ForgottenPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ForgottenPasswordTokenRepository extends JpaRepository<ForgottenPasswordToken, UUID> {
    List<ForgottenPasswordToken> findForgottenPasswordTokensByEmailEqualsAndIssueNotNullAndIssueAfter(String email, Date expiry_date);
}
