package org.example;


import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;


@Component
public class TelegramBot extends TelegramLongPollingBot {
    // zh, evan, mervyn, jy, pong, igy, raymond
    public static final String[] allowedIds = {"260987722", "951962899", "1373801804", "138693338", "773474769", "673595156", "181233098", "907338890"};

    public static final String BOT_USERNAME = "acow123_bot";
    public static final String adarshId = "1032794070";
    public static final HashMap<String, String> groupPrefixes = new HashMap<String, String>();
    private final HashMap<String, String> userGroupMapping = new HashMap<>();
    private final HashMap<String, LinkedList<String>> chatHistory = new HashMap<>();
    private HashMap<String, String> pendingReplies = new HashMap<>();
    private HashMap<String, Integer> forwardedMessageIds = new HashMap<>();
    private HashMap<String, Long> forwardedChatIds = new HashMap<>();
    private Boolean isSilenced = false;

    private static int rngNum = 100;

    @Autowired
    private Properties properties;

    @Autowired
    private OpenAiClient openAiClient;
    @PostConstruct
    public void post() {

        groupPrefixes.put("sn", "-1002065075801");
        groupPrefixes.put("ihg", "-1002095927754");
        groupPrefixes.put("retirement", "-1002079578384");
        groupPrefixes.put("test", "-4183226315");
        groupPrefixes.put("b3new", "-1002237411325");
        groupPrefixes.put("interest", "-1002168874201");

        for (String id: groupPrefixes.values()) {
            LinkedList<String> ll = new LinkedList<>();
            chatHistory.put(id, ll);
        }


        System.out.println("acow123 bot service started");
    }

    @Override
    public String getBotUsername() {
        return "acow123_bot";
    }

    @Override
    public String getBotToken() {
        return properties.getTelegramAPIKey();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        String userId = message.getFrom().getId().toString();
        String user = message.getFrom().getUserName();
        String groupPrefix = userGroupMapping.get(user);
        String groupId = groupPrefixes.get(groupPrefix);
//        System.out.println("ChatID " + chatId);
        if (!isAuthorized(chatId) && !(userId.equals(adarshId))) {
            System.out.println("unauthorised " + chatId + " " + user);
            return;
        }

        //silence bot
        if (userId.equals(adarshId) && (chatId.equals(groupPrefixes.get("sn")) || chatId.equals(groupPrefixes.get("retirement"))) && isSilenced) {
            System.out.println("Silencing Adarsh");
            if (message.hasText()) {
                String messageContent = message.getText();
                DeleteMessage toDelete = new DeleteMessage();
                toDelete.setChatId(chatId);
                toDelete.setMessageId(message.getMessageId());
                try {
                    execute(toDelete);
                } catch (TelegramApiException e) {
                    e.printStackTrace(); // Log the exception
                }
                sendResponse(chatId, messageContent);
            }
            return;
        }

        // gpt bot
        if (chatId.startsWith("-")) {
            if (!isDirectedAtBot(message)) {
                //randomly reply some messages
                if (!isContinueToSend(chatId)) {
                    return;
                }
            }
            String messageContent = message.getText();
            System.out.println("message: " + messageContent);
            if (messageContent.length() > 1000) {
                System.out.println("too long");
                return;
            }
            try {
                LinkedList<String> chat = chatHistory.get(chatId);
                String openAiResponse = openAiClient.getResponse(messageContent, chat);
                chat.add(messageContent);
                chat.add(openAiResponse);
                while (chat.size() > 10) {
                    chat.removeFirst();
                }
                sendResponse(chatId, openAiResponse);
            } catch (Exception e) {
                System.out.println("Exception occured: " + e.toString());
                return;
            }
            return;
        }

        if (message.hasText()) {
            String messageContent = message.getText();
            System.out.println("Text message received from " + chatId
                    + ", user: " + user
                    + ", message: " + messageContent);
            if (messageContent.startsWith("/")) {
                handleCommand(chatId, user, messageContent, message);
                return;
            }

            if (!userGroupMapping.containsKey(user)) {
                sendResponse(chatId, "Please select a group by using a command like /divine or /sn before sending messages.\n\n Do /help for more groups!");
                return;
            }

            sendResponse(groupId, messageContent);
        } else if (message.hasVideo()) {
            System.out.println("Video received from " + chatId
                    + ", user: " + user
                    + ", videoId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasVoice()) {
            System.out.println("Voice message received from " + chatId
                    + ", user: " + user
                    + ", voicemessageId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasSticker()) {
            System.out.println("Sticker received from " + chatId
                    + ", user: " + user
                    + ", stickerId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasDocument()) {
            System.out.println("Document received from " + chatId
                    + ", user: " + user
                    + ", documentId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasPhoto()) {
            System.out.println("Photo received from " + chatId
                    + ", user: " + user
                    + ", photoId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasVideoNote()) {
            System.out.println("Telebubble received from " + chatId
                    + ", user: " + user
                    + ", telebubbleId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        } else if (message.hasPoll()) {
            System.out.println("Poll received from " + chatId
                    + ", user: " + user
                    + ", pollId: " + message.getMessageId());
            forwardMessage(groupId, chatId, String.valueOf(message.getMessageId()));
        }
    }

    public void sendUserGuide(String chatId) {
        StringBuilder guide = new StringBuilder();
        guide.append("\uD83D\uDC76\uD83C\uDFFE **Welcome to the Adarsh is a lil nigger User Guide!** \uD83D\uDC76\uD83C\uDFFE\n\n");
        guide.append("Here are some commands you can use:\n");
        guide.append("/help - Display this user guide.\n");
        guide.append("/list - List out the groups for you to choose!\n");
        guide.append("/silence or /unsilence to MUTE ADARSH!");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(guide.toString());
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
        }
    }

    public void sendGroupList(String chatId) {
        StringBuilder list = new StringBuilder();
        list.append("Available groups are:\n");
        groupPrefixes.keySet().forEach(group -> list.append("/").append(group).append("\n"));

        sendResponse(chatId, list.toString());
    }

    private void handleCommand(String chatId, String user, String command, Message message) {
        if (command.equals("/help")) {
            sendUserGuide(chatId);
            return;
        } else if (command.equals("/list")) {
            sendGroupList(chatId);
            return;
        } else if (command.equals("/silence")) {
            isSilenced = true;
            sendResponse(chatId, "Adarsh has been silenced!");
            return;
        } else if (command.equals("/unsilence")) {
            isSilenced = false;
            sendResponse(chatId, "Adarsh has been given a voice :(");
            return;
        } else if (command.equals("/reply")) {
            sendResponse(chatId, "Forward the message you want to from a valid group chat!");
            pendingReplies.put(chatId, "pending");
            System.out.println(pendingReplies);
            return;
        } else if (command.startsWith("/add")) {
            if (!Objects.equals(chatId, "260987722") && !Objects.equals(chatId, "773474769")) {
                return;
            }
            String[] messageWords = command.split(" ");
            if (messageWords.length > 3) {
                sendResponse(chatId, "invalid message format");
                return;
            }
            String groupName = messageWords[1];
            String groupId = messageWords[2];
            groupPrefixes.put(groupName, groupId);
            LinkedList<String> ll = new LinkedList<>();
            chatHistory.put(groupId, ll);
            sendResponse(chatId, "Group added");
            return;
        } else if (command.startsWith("/remove")) {
            if (!Objects.equals(chatId, "260987722") && !Objects.equals(chatId, "773474769")) {
                return;
            }
            String[] messageWords = command.split(" ");
            if (messageWords.length > 2) {
                sendResponse(chatId, "invalid message format");
                return;
            }
            String groupName = messageWords[1];
            if (!groupPrefixes.containsKey(groupName)) {
                sendResponse(chatId, "group doesnt exist");
                return;
            }
            String groupId = groupPrefixes.get(groupName);
            groupPrefixes.remove(groupName);
            chatHistory.remove(groupId);
            sendResponse(chatId, "Group removed");
            return;
        } else if (command.startsWith("/updateRNG")) {
            if (!Objects.equals(chatId, "260987722") && !Objects.equals(chatId, "773474769")) {
                return;
            }
            String[] messageWords = command.split(" ");
            if (messageWords.length > 2) {
                sendResponse(chatId, "invalid message format");
                return;
            }
            try {
                rngNum = Integer.parseInt(messageWords[1]);
                sendResponse(chatId, "Updated to " + rngNum);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer input");
            }
            return;
        }


        String[] parts = command.split(" ");
        if (parts.length != 1) {
            sendResponse(chatId, "Invalid command format.");
            sendUserGuide(chatId);
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
    private void storeForwardedMessage(Message message) {
        System.out.println("pending");
        String chatId = message.getChatId().toString();
        Long forwardedChatId = message.getForwardFromChat().getLinkedChatId();
        Integer forwardedMessageId = message.getForwardFromMessageId();
        System.out.println(forwardedChatId);
        System.out.println(forwardedMessageId);
        forwardedChatIds.put(chatId, forwardedChatId);
        forwardedMessageIds.put(chatId, forwardedMessageId);
        pendingReplies.remove(chatId);
    }

    // Method to send the response as a reply to the original message
    private void sendResponseToOriginalMessage(String responseText, String chatId) {
        Long forwardedChatId = forwardedChatIds.get(chatId);
        Integer forwardedMessageId = forwardedMessageIds.get(chatId);
        sendResponse(forwardedChatId.toString(), responseText, forwardedMessageId);
        // Clean up
        forwardedChatIds.remove(chatId);
        forwardedMessageIds.remove(chatId);
//        pendingReplies.remove(chatId);
    }
    public void forwardMessage(String chatId, String fromChatId, String messageId) {
        CopyMessage copyMessage = new CopyMessage();
        copyMessage.setChatId(chatId);
        copyMessage.setFromChatId(fromChatId);
        copyMessage.setMessageId(Integer.parseInt(messageId)); // Convert messageId to integer

        try {
            execute(copyMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // Log the exception
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
    public void sendResponse(String chatId, String messageText, Integer replyToMessageId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        message.setReplyToMessageId(replyToMessageId);
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
        if (groupPrefixes.containsValue(chatId)) {
            return true;
        }
        return false;
    }

    private boolean isDirectedAtBot(Message message) {
        return isTaggingBot(message) || isReplyingToBot(message);
    }

    private boolean isTaggingBot(Message message) {
        if (message.hasEntities()) {
            for (MessageEntity entity : message.getEntities()) {
                if ("mention".equals(entity.getType()) &&
                        ("@" + BOT_USERNAME).equals(message.getText().substring(entity.getOffset(), entity.getOffset() + entity.getLength()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isReplyingToBot(Message message) {
        return message.isReply() && message.getReplyToMessage().getFrom().getUserName().equals(BOT_USERNAME);
    }

    private boolean isContinueToSend(String chatId) {
        Random random = new Random();
        int generatedNum = random.nextInt(rngNum);
        int threshold = -1;
        if (chatId.equals(groupPrefixes.get("test"))) {
            threshold = 50;
        } else if (chatId.equals(groupPrefixes.get("retirement"))) {
            threshold = 3;
        }
        return generatedNum < threshold;
    }
}