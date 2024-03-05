package org.example;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
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
import java.util.stream.Collectors;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    // zh, evan, mervyn, jy, pong, igy, raymond
    public static final String[] allowedIds = {"260987722", "951962899", "1373801804", "138693338", "773474769", "673595156", "181233098"};

    //divine stampede
    public static final String[] groupsToForward = {"-994335605", "-1002065075801"};
    public static final HashMap<String, String> groupPrefixes = new HashMap<String, String>();
    private final HashMap<String, String> userGroupMapping = new HashMap<>();

    @Autowired
    private Properties properties;
    @PostConstruct
    public void post() {
//        groupPrefixes.put("test", "-4183226315");
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
        String groupPrefix = userGroupMapping.get(user);
        String groupId = groupPrefixes.get(groupPrefix);

        // Check message type
        if (message.hasText()) {
            String messageContent = message.getText();
            System.out.println("Text message received from " + chatId
                    + ", user: " + user
                    + ", message: " + messageContent);

            // Handle commands
            if (messageContent.startsWith("/")) {
                handleCommand(chatId, user, messageContent);
                return;
            }

            // Check if user has a group set, if not send a message
            if (!userGroupMapping.containsKey(user)) {
                sendResponse(chatId, "Please select a group by using a command like /divine or /sn before sending messages.\n\n Do /help for more groups!");
                return;
            }

            // Forward text message to the group associated with the user
            sendResponse(groupId, messageContent);
        } else if (message.hasVideo()) {
            // Handle video message
            Video video = message.getVideo();
            InputFile videoFile = new InputFile(video.getFileId());
            sendVideo(groupId, videoFile);
        } else if (message.hasSticker()) {
            // Handle sticker message
            Sticker sticker = message.getSticker();
            InputFile stickerFile = new InputFile(sticker.getFileId());
            sendSticker(groupId, stickerFile);
        }
    }


    private void handleCommand(String chatId, String user, String command) {
        if (command.equals("/help")) {
            String formattedGroups = groupPrefixes.keySet().stream()
                    .map(group -> "/" + group)
                    .collect(Collectors.joining(", "));
            sendResponse(chatId, "Available groups:\n" + formattedGroups);
            return;
        }
        String[] parts = command.split(" ");
        if (parts.length != 1) {
            sendResponse(chatId, "Invalid command format. Use commands like /test or /sn.");
            return;
        }

        String newGroup = parts[0].substring(1).toLowerCase(); // Remove the '/' character
        if (groupPrefixes.containsKey(newGroup)) {
            userGroupMapping.put(user, newGroup);
            sendResponse(chatId, "Group set to " + newGroup);
        } else {
            sendResponse(chatId, "Group not found. Available groups are: " + String.join(", ", groupPrefixes.keySet()));
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

    public void sendVideo(String chatId, InputFile file) {
        SendVideo message = new SendVideo();
        message.setChatId(chatId);
        message.setVideo(file);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public void sendSticker(String chatId, InputFile file) {
        SendSticker message = new SendSticker();
        message.setChatId(chatId);
        message.setSticker(file);

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

