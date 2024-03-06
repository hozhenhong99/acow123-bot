package org.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
        groupPrefixes.put("test", "-4183226315");
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
                System.out.println("Group message received from " + chatId);
            }
            return;
        }
        String user = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        String groupPrefix = userGroupMapping.get(user);
        String groupId = groupPrefixes.get(groupPrefix);

        if (message.hasText()) {
            String messageContent = message.getText();
            System.out.println("Text message received from " + chatId
                    + ", user: " + user
                    + ", message: " + messageContent);

            if (messageContent.startsWith("/")) {
                handleCommand(chatId, user, messageContent);
                return;
            }

            if (!userGroupMapping.containsKey(user)) {
                sendResponse(chatId, "Please select a group by using a command like /divine or /sn before sending messages.\n\n Do /help for more groups!");
                return;
            }

            sendResponse(groupId, messageContent);
        } else if (message.hasVideo()) {
            Video video = message.getVideo();
            InputFile videoFile = new InputFile(video.getFileId());
            String caption = message.getCaption();
            System.out.println("Video message received from " + chatId
                    + ", user: " + user
                    + ", videoId: " + video.getFileId());
            sendVideo(groupId, videoFile, caption);
        } else if (message.hasVoice()) {
            Voice voice = message.getVoice();
            InputFile voiceFile = new InputFile(voice.getFileId());
            String caption = message.getCaption(); // Extract caption
            System.out.println("Voice message received from " + chatId
                    + ", user: " + user
                    + ", voiceId: " + voice.getFileId());
            sendVoice(groupId, voiceFile, caption);
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            InputFile stickerFile = new InputFile(sticker.getFileId());
            System.out.println("Sticker message received from " + chatId
                    + ", user: " + user
                    + ", stickerId: " + sticker.getFileId());
            sendSticker(groupId, stickerFile);
        } else if (message.hasDocument()) {
            // Handle document message
            Document document = message.getDocument();
            InputFile documentFile = new InputFile(document.getFileId());
            System.out.println("Document message received from " + chatId
                    + ", user: " + user
                    + ", documentId: " + document.getFileId());
            sendDocument(groupId, documentFile);
        } else if (message.hasVideoNote()) {
            VideoNote videonote = message.getVideoNote();
            InputFile videoFile = new InputFile(videonote.getFileId());
            System.out.println("Document message received from " + chatId
                    + ", user: " + user
                    + ", documentId: " + videonote.getFileId());
            sendVideoNote(groupId, videoFile);
        } else if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize photo = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null);
            if (photo != null) {
                InputFile photoFile = new InputFile(photo.getFileId());
                String caption = message.getCaption();
                System.out.println("Photo message received from " + chatId
                        + ", user: " + user
                        + ", photoId: " + photo.getFileId());
                sendPhoto(groupId, photoFile, caption);
            }
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

    public void sendPhoto(String chatId, InputFile file, String caption) {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setPhoto(file);
        message.setCaption(caption); // Set caption

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public void sendVideo(String chatId, InputFile file, String caption) {
        SendVideo message = new SendVideo();
        message.setChatId(chatId);
        message.setVideo(file);
        message.setCaption(caption); // Set caption

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }
    public void sendVideoNote(String chatId, InputFile file) {
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

    public void sendDocument(String chatId, InputFile file) {
        SendDocument message = new SendDocument();
        message.setChatId(chatId);
        message.setDocument(file);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }
    public void sendVoice(String chatId, InputFile file, String caption) {
        SendVoice message = new SendVoice();
        message.setChatId(chatId);
        message.setVoice(file);
        message.setCaption(caption);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log
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

