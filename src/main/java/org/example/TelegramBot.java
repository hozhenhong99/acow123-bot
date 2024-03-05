package org.example;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    // zh, evan, mervyn, jy, pong, igy, raymond
    public static final String[] allowedIds = {"260987722", "951962899", "1373801804", "138693338", "773474769", "673595156", "181233098"};

    //divine stampede
    public static final String[] groupsToForward = {"-994335605", "-1002065075801"};
    public static final HashMap<String, String> groupPrefixes = new HashMap<String, String>();


    @Autowired
    private Properties properties;
    @PostConstruct
    public void post() {
        groupPrefixes.put("divine", "-994335605");
        groupPrefixes.put("sn", "-1002065075801");
        groupPrefixes.put("recre", "-1001927647862");
        groupPrefixes.put("ihg", "-1002095927754");
        groupPrefixes.put("retirement", "-4070951855");


        System.out.println("acow123 bot service started");
    }

    @Override
    public String getBotUsername() {
        return "zh_strava_bot";
    }

    @Override
    public String getBotToken() {
        return properties.getTelegramAPIKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = String.valueOf(message.getChatId());
        if (chatId.startsWith("-")) {
            if (!groupPrefixes.containsValue(chatId)) {
                System.out.println("group message received from " + chatId);
            }
            return;
        }
        String user = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        String messageContent = message.getText();

        System.out.println("message received from " + chatId
                + ", user: " + user
                + ", message: " + messageContent);

        if (!isAuthorized(chatId)) {
            sendResponse(chatId, "hello " + firstName +"! i am adarsh and i thank you for your message :)");
            return;
        }

        String[] parts = messageContent.split(" ");
        if (groupPrefixes.containsKey(parts[0])) {
            String messageToSend = messageContent.substring(parts[0].length() + 1); //space character
            sendResponse(groupPrefixes.get(parts[0]), messageToSend);
        } else {
            for(String groupId : groupsToForward) {
                sendResponse(groupId, messageContent);
            }
        }
    }

    public void sendResponse(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public boolean isAuthorized(String chatId) {
        for(String element : allowedIds) {
            if(element.equals(chatId)) {
                return true;
            }
        }
        return false;
    }
}

