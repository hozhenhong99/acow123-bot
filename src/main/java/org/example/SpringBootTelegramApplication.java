package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(Properties.class)
public class SpringBootTelegramApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootTelegramApplication.class, args);
    }
}