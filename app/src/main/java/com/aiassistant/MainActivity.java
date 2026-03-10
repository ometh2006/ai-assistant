package com.aiassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText inputField;
    private ImageButton sendButton, voiceButton;

    private GroqApiClient groqClient;
    private CommandEngine commandEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        groqClient = new GroqApiClient(this);
        commandEngine = new CommandEngine(this);

        recyclerView = findViewById(R.id.recyclerView);
        inputField  = findViewById(R.id.inputField);
        sendButton  = findViewById(R.id.sendButton);
        voiceButton = findViewById(R.id.voiceButton);

        chatAdapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Greeting message
        addBotMessage("Hello! I'm your AI Assistant. Ask me anything or say a command like 'open camera' or 'turn on flashlight'.");

        sendButton.setOnClickListener(v -> {
            String text = inputField.getText().toString().trim();
            if (!text.isEmpty()) {
                processUserInput(text);
                inputField.setText("");
            }
        });

        voiceButton.setOnClickListener(v -> checkMicPermissionAndListen());
    }

    // ─── Voice Input ──────────────────────────────────────────────
    private void checkMicPermissionAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            startSpeechRecognition();
        }
    }

    private void startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                processUserInput(spokenText);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        }
    }

    // ─── Core Processing ──────────────────────────────────────────
    private void processUserInput(String input) {
        addUserMessage(input);

        // 1. Try local device command first (fast, offline)
        String commandResult = commandEngine.handleCommand(input);
        if (commandResult != null) {
            addBotMessage(commandResult);
            return;
        }

        // 2. Send to Groq AI for general queries
        addBotMessage("Thinking...");
        groqClient.sendMessage(input, new GroqApiClient.Callback() {
            @Override
            public void onResponse(String reply) {
                runOnUiThread(() -> {
                    // Replace "Thinking..." with real reply
                    messages.remove(messages.size() - 1);
                    addBotMessage(reply);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    messages.remove(messages.size() - 1);
                    addBotMessage("⚠️ Error: " + error + "\nCheck your internet connection.");
                });
            }
        });
    }

    // ─── Chat Helpers ─────────────────────────────────────────────
    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, ChatMessage.TYPE_BOT));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }
}
