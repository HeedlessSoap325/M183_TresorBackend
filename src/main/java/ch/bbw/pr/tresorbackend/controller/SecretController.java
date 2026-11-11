package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.model.*;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * SecretController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {
    private final SecretService secretService;
    private final UserService userService;

    // create secret REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/")
    public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret) {
        User user = userService.findByEmail(newSecret.getEmail());

        //transfer secret and encrypt content
        Secret secret = new Secret(
            null,
            user.getId(),
            new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt()).encrypt(newSecret.getContent().toString())
        );

        //save secret in db
        secretService.createSecret(secret);
        TresorbackendApplication.getLogger().info("SecretController.createSecret, secret saved in db");

        JsonObject obj = new JsonObject();
        obj.addProperty("answer", "Secret saved");
        String json = new Gson().toJson(obj);

        TresorbackendApplication.getLogger().info("SecretController.createSecret {}", json);
        return ResponseEntity.accepted().body(json);
    }

    // Build Get Secrets by userId REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/byuserid")
    public ResponseEntity<List<Secret>> getSecretsByUserId(@Valid @RequestBody EncryptCredentials credentials) {
        TresorbackendApplication.getLogger().info("SecretController.getSecretsByUserId: EncryptCredentials(userId = {}, email = {})", credentials.getUserId(), credentials.getEmail());

        User user = userService.getUserById(credentials.getUserId());

        List<Secret> secrets = secretService.getSecretsByUserId(credentials.getUserId());
        if (secrets.isEmpty()) {
            TresorbackendApplication.getLogger().info("SecretController.getSecretsByUserId: secret is empty");
            return ResponseEntity.ok(secrets);
        }

        // Decrypt content
        for(Secret secret: secrets) {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword(), user.getSalt()).decrypt(secret.getContent()));
        }

        TresorbackendApplication.getLogger().info("SecretController.getSecretsByUserId: successfully decrypted all user secrets");
        return ResponseEntity.ok(secrets);
    }

    // Build Get Secrets by email REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PostMapping("/byemail")
    public ResponseEntity<List<Secret>> getSecretsByEmail(@Valid @RequestBody EncryptCredentials credentials) {
        TresorbackendApplication.getLogger().info("SecretController.getSecretsByEmail: EncryptCredentials(userId = {}, email = {})", credentials.getUserId(), credentials.getEmail());

        User user = userService.findByEmail(credentials.getEmail());

        List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
        if (secrets.isEmpty()) {
            TresorbackendApplication.getLogger().info("SecretController.getSecretsByEmail: secret is empty");
            return ResponseEntity.ok(secrets);
        }

        // Decrypt content
        for(Secret secret: secrets) {
            secret.setContent(new EncryptUtil(credentials.getEncryptPassword(), user.getSalt()).decrypt(secret.getContent()));
        }

        TresorbackendApplication.getLogger().info("SecretController.getSecretsByEmail: successfully decrypted all user secrets");
        return ResponseEntity.ok(secrets);
    }

    // Build Get All Secrets REST API
    // http://localhost:8080/api/secrets
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @GetMapping("/")
    public ResponseEntity<List<Secret>> getAllSecrets() {
        List<Secret> secrets = secretService.getAllSecrets();
        return ResponseEntity.status(HttpStatus.OK).body(secrets);
    }

    // Build Update Secrete REST API
    // http://localhost:8080/api/secrets/1
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @PutMapping("{id}")
    public ResponseEntity<String> updateSecret(@PathVariable("id") Long secretId, @Valid @RequestBody NewSecret newSecret) {
        // get Secret with id
        Secret dbSecrete = secretService.getSecretById(secretId);
        User user = userService.findByEmail(newSecret.getEmail());
        if (user == null) return ResponseEntity.notFound().build();

        // check if Secret in db has not same userid
        if(!Objects.equals(dbSecrete.getUserId(), user.getId())){
            TresorbackendApplication.getLogger().info("SecretController.updateSecret: not same user id (secretUserId = {}, userId = {})", dbSecrete.getUserId(), user.getId());

            JsonObject obj = new JsonObject();
            obj.addProperty("answer", "Secret has not same user id");
            String json = new Gson().toJson(obj);
            return ResponseEntity.badRequest().body(json);
        }

        // check if Secret can be decrypted with password
        new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt()).decrypt(dbSecrete.getContent());

        // modify Secret in db.
        Secret secret = new Secret(
            secretId,
            user.getId(),
            new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt()).encrypt(newSecret.getContent().toString())
        );
        secretService.updateSecret(secret);

        // save secret in db
        secretService.createSecret(secret);
        TresorbackendApplication.getLogger().info("SecretController.updateSecret: secret updated in db");

        JsonObject obj = new JsonObject();
        obj.addProperty("answer", "Secret updated");
        String json = new Gson().toJson(obj);
        return ResponseEntity.accepted().body(json);
    }

    // Build Delete Secret REST API
    @CrossOrigin(origins = "${dev.heedlesssoap.tresorbackend.cross-origin}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteSecret(@PathVariable("id") Long secretId) {
        // todo: Some kind of brute force delete, perhaps test first userid and encryptpassword
        secretService.deleteSecret(secretId);
        TresorbackendApplication.getLogger().info("SecretController.deleteSecret: successfully deleted Secret(id = {})", secretId);
        return ResponseEntity.ok().body("Secret successfully deleted!");
    }
}
