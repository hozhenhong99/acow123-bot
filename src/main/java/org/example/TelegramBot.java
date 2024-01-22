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

    @Autowired
    private Properties properties;
    @PostConstruct
    public void post() {
        System.out.println("strava bot service started");
    }

    @Override
    public String getBotUsername() {
        return "zh_strava_bot";
    }

    @Override
    public String getBotToken() {
        return properties.getTelegramAPIKey();
//        return "6602618744:AAF-OxgLSNKWx9yB-npMhPwRhxL_f8EMmKs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = String.valueOf(message.getChatId());
        if (String.valueOf(chatId).equals("-1002065075801") || String.valueOf(chatId).equals("-994335605")) {
            return;
        }
        String user = message.getFrom().getUserName();
        String firstName = message.getFrom().getFirstName();
        String messageContent = message.getText();

        System.out.println("message received from " + chatId
                + ", user: " + user
                + ", message: " + messageContent);
//        getActivities();
        sendResponse(chatId, "hello " + firstName +"! Only the chosen one will receive special messages :)");
    }

    public void getActivitiesAndSend() {
        String zhChatId = "260987722"; //zhenhong
        String wlChatId = "273199341"; //wl

        String activities = getActivities();
        JSONArray activitiesArray = new JSONArray(activities);

        for (int i = 0; i < activitiesArray.length(); i++) {
            JSONObject activity = activitiesArray.getJSONObject(i);


            String startDate = activity.getString("start_date_local");
            String formattedStartDate = startDate.replace('T', ' ').replace("Z", "");
            double distance = activity.getDouble("distance")/1000;
            int movingTime = activity.getInt("moving_time");  // moving time in seconds
            double pace = movingTime/distance/60;
            int paceMinutes = (int)pace;
            int paceSeconds = (int)(pace % 1 * 60);
            double avgHeartRate = activity.getDouble("average_heartrate");
            // ... extract other fields as needed

            // Print or process the extracted information
            StringBuilder sb = new StringBuilder();
            sb.append(formattedStartDate);
            sb.append(System.lineSeparator());
            sb.append("Distance: " + distance);
            sb.append(System.lineSeparator());
            sb.append("Pace: " + paceMinutes + ":" + paceSeconds + "min/km");
            sb.append(System.lineSeparator());
            sb.append("Average HR: " + avgHeartRate);

            System.out.println(sb.toString());

            sendResponse(zhChatId, sb.toString());
            sendResponse(wlChatId, sb.toString());
        }

    }

    public String getActivities() {
        try {
            Instant now = Instant.now(); // Current time
            Instant twelveHoursAgo = now.minus(12, ChronoUnit.HOURS); // Time 24 hours ago
            long unixTime12HoursAgo = twelveHoursAgo.getEpochSecond();
            String timeString = String.valueOf(unixTime12HoursAgo);

            URL url = new URL("https://www.strava.com/api/v3/activities?after=" + timeString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");


            String accessToken = GetAccessToken();
            con.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String GetAccessToken() {
        String response = GetRefreshTokenResponse();
        JSONObject jsonObj = new JSONObject(response.toString());
        String accessToken = jsonObj.getString("access_token");
        return accessToken;
    }

    private String GetRefreshTokenResponse() {
        String url = "https://www.strava.com/api/v3/oauth/token";
//        String clientId = "120002";
//        String clientSecret = "41c89eefa09db3f43b0a90f9bb35def3b7887dfb";
//        String refreshToken = "7629e073b83200cce0cc8c6b3aadb2e9acde2a95";
        String clientId = properties.getStravaClientId();
        String clientSecret = properties.getClientSecret();
        String refreshToken = properties.getRefreshToken();
        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setDoOutput(true);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("client_id", clientId);
            arguments.put("client_secret", clientSecret);
            arguments.put("grant_type", "refresh_token");
            arguments.put("refresh_token", refreshToken);
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            connection.setFixedLengthStreamingMode(length);
            connection.connect();
            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                os.write(out);
            }

            // Read the response from the input stream
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception as you prefer
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
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
}

