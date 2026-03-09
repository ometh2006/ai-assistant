package com.aiassistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.AlarmClock;

import java.util.Locale;

/**
 * CommandEngine — Offline Device Control
 * ───────────────────────────────────────
 * Handles device commands WITHOUT internet.
 * Returns null if the input is NOT a recognized command
 * (caller then falls through to the AI API).
 */
public class CommandEngine {

    private final Context context;
    private boolean flashlightOn = false;
    private CameraManager cameraManager;
    private String cameraId;

    public CommandEngine(Context context) {
        this.context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try { cameraId = cameraManager.getCameraIdList()[0]; } catch (Exception ignored) {}
        }
    }

    /**
     * Call this with user's raw input text.
     * @return action result string, or null if not a device command
     */
    public String handleCommand(String input) {
        String cmd = input.toLowerCase(Locale.ENGLISH).trim();

        // ── Flashlight ──────────────────────────────────────────
        if (matches(cmd, "flashlight on", "turn on flashlight", "torch on", "light on")) {
            return toggleFlashlight(true);
        }
        if (matches(cmd, "flashlight off", "turn off flashlight", "torch off", "light off")) {
            return toggleFlashlight(false);
        }
        if (matches(cmd, "toggle flashlight", "flashlight")) {
            return toggleFlashlight(!flashlightOn);
        }

        // ── WiFi ────────────────────────────────────────────────
        if (matches(cmd, "wifi on", "turn on wifi", "enable wifi", "wi-fi on")) {
            return setWifi(true);
        }
        if (matches(cmd, "wifi off", "turn off wifi", "disable wifi", "wi-fi off")) {
            return setWifi(false);
        }
        if (matches(cmd, "wifi settings", "open wifi settings")) {
            openSettings(Settings.ACTION_WIFI_SETTINGS);
            return "Opening WiFi settings...";
        }

        // ── Open Apps ───────────────────────────────────────────
        if (matches(cmd, "open camera", "camera")) {
            return openApp("android.media.action.IMAGE_CAPTURE", "Camera");
        }
        if (matches(cmd, "open gallery", "open photos", "gallery")) {
            return openApp(Intent.ACTION_PICK, "Gallery");
        }
        if (matches(cmd, "open settings", "settings")) {
            openSettings(Settings.ACTION_SETTINGS);
            return "Opening Settings...";
        }
        if (matches(cmd, "open calculator", "calculator", "calc")) {
            return openAppByName("com.android.calculator2", "Calculator");
        }
        if (matches(cmd, "open browser", "browser", "open chrome")) {
            return openUrl("https://www.google.com");
        }
        if (matches(cmd, "open maps", "google maps", "maps")) {
            return openUrl("https://maps.google.com");
        }
        if (matches(cmd, "open youtube", "youtube")) {
            return openUrl("https://www.youtube.com");
        }
        if (matches(cmd, "open whatsapp", "whatsapp")) {
            return openAppByName("com.whatsapp", "WhatsApp");
        }
        if (matches(cmd, "open facebook", "facebook")) {
            return openAppByName("com.facebook.katana", "Facebook");
        }
        if (matches(cmd, "open clock", "clock", "alarm")) {
            return openAppByName("com.android.deskclock", "Clock");
        }
        if (matches(cmd, "open contacts", "contacts")) {
            openSettings(Intent.ACTION_VIEW + "/contacts");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType("vnd.android.cursor.dir/contact");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return "Opening Contacts...";
        }

        // ── Alarm / Timer ───────────────────────────────────────
        if (cmd.contains("set alarm") || cmd.startsWith("alarm at")) {
            return "To set an alarm, opening Clock app...";
            // You can parse hours/minutes from cmd and use AlarmClock intent
        }

        // ── Search ──────────────────────────────────────────────
        if (cmd.startsWith("search for ") || cmd.startsWith("google ")) {
            String query = cmd.replace("search for ", "").replace("google ", "");
            return openUrl("https://www.google.com/search?q=" + Uri.encode(query));
        }

        // Not a device command — return null so AI handles it
        return null;
    }

    // ─── Implementations ──────────────────────────────────────────

    private String toggleFlashlight(boolean on) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return "Flashlight control requires Android 6+. Your device should be supported!";
        }
        try {
            cameraManager.setTorchMode(cameraId, on);
            flashlightOn = on;
            return on ? "🔦 Flashlight ON" : "🔦 Flashlight OFF";
        } catch (CameraAccessException e) {
            return "Could not access flashlight: " + e.getMessage();
        }
    }

    private String setWifi(boolean enable) {
        try {
            WifiManager wifiManager = (WifiManager)
                    context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.setWifiEnabled(enable); // Works on Android 6-9
                return enable ? "📶 WiFi turned ON" : "📶 WiFi turned OFF";
            }
        } catch (Exception e) {
            // Android 10+ blocks programmatic wifi toggle
        }
        // Fallback: open WiFi settings
        openSettings(Settings.ACTION_WIFI_SETTINGS);
        return "Opening WiFi settings (Android 10+ requires manual toggle)";
    }

    private String openApp(String action, String appName) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Opening " + appName + "...";
        } catch (Exception e) {
            return appName + " not found on this device.";
        }
    }

    private String openAppByName(String packageName, String appName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Opening " + appName + "...";
        }
        return appName + " is not installed.";
    }

    private String openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return "Opening browser...";
    }

    private void openSettings(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { context.startActivity(intent); } catch (Exception ignored) {}
    }

    /** Check if input contains any of the given keywords */
    private boolean matches(String input, String... keywords) {
        for (String k : keywords) {
            if (input.contains(k)) return true;
        }
        return false;
    }
}
