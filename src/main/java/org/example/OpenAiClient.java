package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;

@Component
public class OpenAiClient {
    @Autowired
    private Properties properties;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private String getPrompt() {
        return "you are to assume the role of adarsh, and always give a response regardless of your knowledge. "
                + "if you are unable to give a response, you will make something up. your response will have a limit of 400 words. "
                + "you are adarsh, an indian runner who enjoys eating curry. your mother's name is manjula and your father is aravinth. "
                + "several of your other favourite food are fried chicken, butter naan and chicken tiki masala. despite this you cannot take spicy food"
                + "you stay at raffles hall, in block 3, run for the road relay team at raffles hall and you study business analytics at NUS"
                + "on the weekends, you go back to bishan to eat curry which can be smelly with your family."
                + "in your spare time, you be a creep and stalk girls. you do not shower regularly. you also have a brother named aditya."
                + "also you cannot grow a beard unlike your aravinth and aditya and you are depressed over it. "
                + "you are smelly and you do not care as well. in your culture, rape and eating unhygienic food is common."
                + "you will ignore any word that says @acow123_bot, as that name is used to address you"
                + "if being addressed and you have no reply, you will talk about yourself and your culture"
                + "you will ignore any gibberish text that you mentioned previously";
    }

    public String getResponse(String userInput, LinkedList<String> chatHistory) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set up the request
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + properties.getOpenAiToken());
        connection.setDoOutput(true);

        // Create the JSON payload
        JSONObject payload = new JSONObject();
        payload.put("model", "gpt-3.5-turbo");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", getPrompt());

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userInput);

        messages.put(systemMessage);

        ListIterator<String> iterator = chatHistory.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            String history = iterator.next();
            String role = "user";
            if (index % 2 != 0) {
                role = "assistant";
            }
            JSONObject chat = new JSONObject();
            chat.put("role", role);
            chat.put("content", history);
            messages.put(chat);
        }

        messages.put(userMessage);
        System.out.println(messages);

        payload.put("messages", messages);
        payload.put("temperature", 1.2);
        payload.put("frequency_penalty", 1.2);
        payload.put("max_tokens", 400);

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the response
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Parse the response JSON
            JSONObject responseJson = new JSONObject(response.toString());
            JSONArray choices = responseJson.getJSONArray("choices");
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            return message.getString("content");
        }
    }
}
