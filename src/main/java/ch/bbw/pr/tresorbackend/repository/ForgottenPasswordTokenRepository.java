package ch.bbw.pr.tresorbackend.repository;

import ch.bbw.pr.tresorbackend.model.ForgottenPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ForgottenPasswordTokenRepository extends JpaRepository<ForgottenPasswordToken, UUID> {
}
