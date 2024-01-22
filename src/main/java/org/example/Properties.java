package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "app")
public class Properties {
    public String getTelegramAPIKey() {
        return telegramAPIKey;
    }

    public String getStravaClientId() {
        return stravaClientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setTelegramAPIKey(String telegramAPIKey) {
        this.telegramAPIKey = telegramAPIKey;
    }

    public void setStravaClientId(String stravaClientId) {
        this.stravaClientId = stravaClientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Value("telegramAPIKey")
    String telegramAPIKey;
    @Value("stravaClientId")
    String stravaClientId;
    @Value("clientSecret")
    String clientSecret;
    @Value("refreshToken")
    String refreshToken;
}
