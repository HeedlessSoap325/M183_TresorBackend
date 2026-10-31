package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.ConfigProperties;
import ch.bbw.pr.tresorbackend.service.ReCAPTCHAService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
@AllArgsConstructor
public class ReCAPTCHAServiceImpl implements ReCAPTCHAService {
    private final ConfigProperties configProperties;

    @Override
    public boolean verifyCaptcha(String token) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify";
            String params = "secret=" + configProperties.getCaptchaSecretKey() + "&response=" + token;

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(params.getBytes(StandardCharsets.UTF_8));

            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return response.contains("\"success\": true");
        } catch (Exception e) {
            return false;
        }
    }
}
