package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.Secret;
import java.util.List;

/**
 * SecretService
 * @author Peter Rutschmann
 */
public interface SecretService {
    Secret createSecret(Secret secret) throws ApplicationError;

    Secret getSecretById(Long secretId) throws ApplicationError;

    List<Secret> getAllSecrets() throws ApplicationError;

    Secret updateSecret(Secret secret) throws ApplicationError;

    void deleteSecret(Long secretId);

    List<Secret> getSecretsByUserId(Long userId) throws ApplicationError;
}
