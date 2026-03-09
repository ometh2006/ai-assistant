package com.aiassistant;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Groq API Client
 * ───────────────
 * Free tier: 6,000 req/day, 500,000 tokens/day (as of 2024)
 * Get your key at: https://console.groq.com
 * Model used: llama3-8b-8192 (fast + smart, good for low-end devices)
 */
public class GroqApiClient {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama3-8b-8192"; // Fast & free
    private static final MediaType JSON_TYPE = MediaType.parse("application/json");

    // ⚠️  Put your Groq API key here (or store in SharedPreferences via Settings screen)
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";

    private final OkHttpClient httpClient;
    private final List<JSONObject> conversationHistory = new ArrayList<>();
    private final String systemPrompt;

    public interface Callback {
        void onResponse(String reply);
        void onError(String error);
    }

    public GroqApiClient(Context context) {
        httpClient = new OkHttpClient();
        systemPrompt = "You are a helpful, friendly, and concise AI assistant running on a low-end "
                + "Android phone. Keep your answers short and clear. If asked about device tasks "
                + "(like open apps, toggle wifi, flashlight), say those are handled locally. "
                + "Always respond in the same language the user uses.";
    }

    public void sendMessage(String userMessage, Callback callback) {
        // Add user message to history
        try {
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            conversationHistory.add(userMsg);

            // Keep last 10 messages to save memory
            if (conversationHistory.size() > 10) {
                conversationHistory.remove(0);
            }

            // Build request body
            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("max_tokens", 400); // Keep short for low-end phones

            JSONArray messages = new JSONArray();

            // System prompt
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.put(sysMsg);

            // Conversation history
            for (JSONObject msg : conversationHistory) {
                messages.put(msg);
            }

            body.put("messages", messages);

            // Make request
            Request request = new Request.Builder()
                    .url(GROQ_API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(JSON_TYPE, body.toString()))
                    .build();

            httpClient.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);

                        if (!response.isSuccessful()) {
                            // API error (e.g., invalid key, quota exceeded)
                            String errMsg = json.optString("error", "API error " + response.code());
                            callback.onError(errMsg);
                            return;
                        }

                        String reply = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim();

                        // Save assistant reply to history
                        JSONObject assistantMsg = new JSONObject();
                        assistantMsg.put("role", "assistant");
                        assistantMsg.put("content", reply);
                        conversationHistory.add(assistantMsg);

                        callback.onResponse(reply);

                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request error: " + e.getMessage());
        }
    }

    /** Clear conversation memory */
    public void clearHistory() {
        conversationHistory.clear();
    }
}
