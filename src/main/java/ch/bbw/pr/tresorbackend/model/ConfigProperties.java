package ch.bbw.pr.tresorbackend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * ConfigProperties
 * @author Peter Rutschmann
 */
@Component
@ConfigurationProperties("dev.heedlesssoap.tresorbackend")
@Getter
@Setter
@Validated
public class ConfigProperties {
    @NotBlank(message = "Please provide the url of your frontend in applications.properties be setting 'dev.heedlesssoap.tresorbackend.crossOrigin' to the url")
    private String crossOrigin;

    @NotBlank(message = "Please provide a secure and random pepper in applications.properties be setting 'dev.heedlesssoap.tresorbackend.pepper' to the pepper")
    private String pepper;

    @NotBlank(message = "Please provide your captcha secret key in applications.properties be setting 'dev.heedlesssoap.tresorbackend.captchaSecretKey' to the key")
    private String captchaSecretKey;

    @NotNull(message = "Please specify whether or not to send mail in application.properties by setting 'dev.heedlesssoap.tresorbackend.sendMail' to true or false")
    private Boolean sendMail;
}
