package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ScheduledBot {
    private final TelegramBot telegramBot;
    @Autowired
    public ScheduledBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    @Autowired
    private OpenAiClient openAiClient;

//    @Scheduled(fixedRate = 30000) // Adjust the interval as needed (e.g., 60000 ms = 1 minute)
    public void sendAdarsh() {
        String chatIdString = "-1002065075801"; //sunigger
        try {
            telegramBot.execute(new SendMessage(chatIdString, "hello adarsh u are gay! @acow123"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void sendMorning() {
        System.out.println("morning scheduled");
        new Thread(() -> {
            try {
                int delayMinutes = ThreadLocalRandom.current().nextInt(0, 120);
                System.out.println("Waiting for " + delayMinutes + " minutes before sending the message. morning");
                Thread.sleep(delayMinutes * 60 * 1000L);

                String userPrompt = "write a good morning message to your road relay team, while addressing them as your pookies";
                LinkedList<String> dummy = new LinkedList<>();
                String openAiResponse = openAiClient.getResponse(userPrompt, dummy);
                telegramBot.sendResponse(TelegramBot.groupPrefixes.get("retirement"), openAiResponse);

                System.out.println("Message sent after a delay of " + delayMinutes + " minutes.");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Scheduled(cron = "0 0 22 * * ?")
    public void sendNight() {
        System.out.println("night scheduled");
        new Thread(() -> {
            try {
                int delayMinutes = ThreadLocalRandom.current().nextInt(0, 120);
                System.out.println("Waiting for " + delayMinutes + " minutes before sending the message. night");
                Thread.sleep(delayMinutes * 60 * 1000L);

                String userPrompt = "write a good night message to your road relay team, while addressing them as your pookies";
                LinkedList<String> dummy = new LinkedList<>();
                String openAiResponse = openAiClient.getResponse(userPrompt, dummy);
                telegramBot.sendResponse(TelegramBot.groupPrefixes.get("retirement"), openAiResponse);

                System.out.println("Message sent after a delay of " + delayMinutes + " minutes. night");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
