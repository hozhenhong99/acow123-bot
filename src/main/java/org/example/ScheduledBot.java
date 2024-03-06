package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class ScheduledBot {
    private final TelegramBot telegramBot;
    @Autowired
    public ScheduledBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

//    @Scheduled(fixedRate = 30000) // Adjust the interval as needed (e.g., 60000 ms = 1 minute)
    public void sendAdarsh() {
        String chatIdString = "-994335605"; //miracle
//        String chatIdString = "-1002065075801"; //sunigger
        try {
            telegramBot.execute(new SendMessage(chatIdString, "hello adarsh u are gay! @acow123"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
