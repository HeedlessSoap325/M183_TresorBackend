package ch.bbw.pr.tresorbackend.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final RestClient restClient = RestClient.create();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
        throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(userRequest);

        String provider = userRequest
            .getClientRegistration()
            .getRegistrationId();

        String email;
        String name;
        String providerId;

        switch (provider) {
            case "google" -> {
                email = oauthUser.getAttribute("email");
                name = oauthUser.getAttribute("name");
                providerId = oauthUser.getAttribute("sub");

                Boolean verified = oauthUser.getAttribute("email_verified");

                if (!Boolean.TRUE.equals(verified)) {
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("unverified_email"),
                        "Google email is not verified"
                    );
                }
            }

            case "github" -> {
                email = getGitHubEmail(userRequest);
                name = oauthUser.getAttribute("name");
                providerId = Integer.toString(oauthUser.getAttribute("id"));

                if (name == null) {
                    name = oauthUser.getAttribute("login");
                }

                if (email == null) {
                    throw new OAuth2AuthenticationException(
                        new OAuth2Error("missing_email"),
                        "GitHub did not provide an email address"
                    );
                }
            }

            default -> throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider"),
                "Unsupported OAuth2 provider: " + provider
            );
        }

        Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());

        attributes.put("email", email);
        attributes.put("name", name);
        attributes.put("sub", providerId);

        return new DefaultOAuth2User(
            oauthUser.getAuthorities(),
            attributes,
            provider.equals("github") ? "id" : "sub"
        );
    }

    private String getGitHubEmail(OAuth2UserRequest userRequest) {
        List<Map<String, Object>> emails = restClient.get()
            .uri("https://api.github.com/user/emails")
            .headers(headers -> {
                headers.setBearerAuth(
                    userRequest.getAccessToken().getTokenValue()
                );
                headers.setAccept(
                    List.of(MediaType.APPLICATION_JSON)
                );
            })
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        if (emails == null) {
            return null;
        }

        return emails.stream()
            .filter(e -> Boolean.TRUE.equals(e.get("primary")))
            .filter(e -> Boolean.TRUE.equals(e.get("verified")))
            .map(e -> (String) e.get("email"))
            .findFirst()
            .orElse(null);
    }


    /*
    private User createUser(
        String email,
        String name,
        String googleId
    ) {
    }
     */
}
