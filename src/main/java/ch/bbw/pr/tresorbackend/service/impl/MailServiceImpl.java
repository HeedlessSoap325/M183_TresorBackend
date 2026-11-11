package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.TresorbackendApplication;
import ch.bbw.pr.tresorbackend.exception.ApplicationError;
import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final ConfigProperties configProperties;

    @Override
    public void sendPasswordResetHTML(String resetLink, String resetLinkExpirationTime, User user) throws ApplicationError {
        if (!configProperties.getSendMail()) {
            TresorbackendApplication.getLogger().info("MailServiceImpl.sendPasswordResetHTML: sending mail is disabled.");
            return;
        }

        Context context = new Context();
        context.setVariable("firstname", user.getFirstName());
        context.setVariable("lastname", user.getLastName());
        context.setVariable("resetLink", resetLink);
        context.setVariable("expire_time", resetLinkExpirationTime);

        String html = templateEngine.process("password-reset", context);
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(user.getEmail());
            helper.setFrom("TresorApplikation@heedlesssoap.dev", "TresorApplikation");
            helper.setSubject("Reset your password");
            helper.setText(html, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            TresorbackendApplication.getLogger().error("MailServiceImpl.sendPasswordResetHTML: failed to set mail metadata: {}", e.getMessage());
            throw new ApplicationError(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send mail");
        }

        javaMailSender.send(message);
    }
}
