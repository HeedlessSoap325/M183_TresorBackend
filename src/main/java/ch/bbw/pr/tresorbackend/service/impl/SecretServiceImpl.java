package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.repository.SecretRepository;
import ch.bbw.pr.tresorbackend.service.SafeDbCall;
import ch.bbw.pr.tresorbackend.service.SecretService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SecretServiceImpl
 * @author Peter Rutschmann
 */
@Service
@AllArgsConstructor
public class SecretServiceImpl implements SecretService {
    private SecretRepository secretRepository;

    @Override
    public Secret createSecret(Secret secret) throws ApplicationError {
          Secret savedSecret = SafeDbCall.safeDbCall(()-> secretRepository.save(secret), null);
          if (savedSecret == null) {
                throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save secret");
          }

          return savedSecret;
    }

    @Override
    public Secret getSecretById(Long secretId) throws ApplicationError {
          Optional<Secret> optionalSecret = SafeDbCall.safeDbCall(() -> secretRepository.findById(secretId), Optional.empty());
          if  (optionalSecret.isEmpty()) {
                throw new ApplicationError(HttpStatus.NOT_FOUND, "Secret not found");
          }
          return optionalSecret.get();
    }

    @Override
    public List<Secret> getAllSecrets() {
        return SafeDbCall.safeDbCall(() -> secretRepository.findAll(), List.of());
    }

    @Override
    public Secret updateSecret(Secret secret) throws ApplicationError {
          Optional<Secret> optionalSecret = SafeDbCall.safeDbCall(() -> secretRepository.findById(secret.getId()), Optional.empty());
          if (optionalSecret.isEmpty()) {
                throw new ApplicationError(HttpStatus.NOT_FOUND, "Secret not found");
          }
          Secret existingSecret = optionalSecret.get();

          existingSecret.setUserId(secret.getUserId());
          existingSecret.setContent(secret.getContent());
          Secret updatedSecret = SafeDbCall.safeDbCall(() -> secretRepository.save(existingSecret), null);
          if (updatedSecret == null) {
                throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save secret");
          }

          return updatedSecret;
    }

    @Override
    public void deleteSecret(Long secretId) {
        SafeDbCall.safeDbCall(() -> secretRepository.deleteById(secretId));
    }

    @Override
    public List<Secret> getSecretsByUserId(Long userId) throws ApplicationError {
        List<Secret> secrets = SafeDbCall.safeDbCall(() -> secretRepository.findByUserId(userId), null);
        if (secrets == null) {
            throw new ApplicationError(HttpStatus.NOT_FOUND, "Secrets not found");
        }

        return secrets;
    }
}
