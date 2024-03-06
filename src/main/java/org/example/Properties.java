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

    public void setTelegramAPIKey(String telegramAPIKey) {
        this.telegramAPIKey = telegramAPIKey;
    }

    @Value("telegramAPIKey")
    String telegramAPIKey;
}
