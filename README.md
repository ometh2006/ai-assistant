# 🤖 AI Assistant — Android App
**Lightweight AI assistant for Huawei Y3 2017 (Android 6.0+)**

---

## 📦 What's Inside

```
AIAssistant/
├── app/src/main/
│   ├── java/com/aiassistant/
│   │   ├── MainActivity.java      ← Main UI + voice button logic
│   │   ├── GroqApiClient.java     ← Groq AI API integration
│   │   ├── CommandEngine.java     ← Offline device commands
│   │   ├── ChatMessage.java       ← Data model
│   │   └── ChatAdapter.java       ← RecyclerView adapter
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml  ← Main chat UI
│   │   │   ├── item_message_user.xml
│   │   │   └── item_message_bot.xml
│   │   ├── drawable/              ← Chat bubbles, button shapes
│   │   └── values/styles.xml
│   └── AndroidManifest.xml        ← Permissions
├── app/build.gradle               ← Dependencies
└── build.gradle
```

---

## 🚀 Step-by-Step Setup

### Step 1 — Install Android Studio
Download from: https://developer.android.com/studio
- Free, works on Windows/Mac/Linux
- Use the latest stable version

### Step 2 — Create a New Project
1. Open Android Studio → "New Project"
2. Choose **"Empty Activity"**
3. Set:
   - Name: `AI Assistant`
   - Package name: `com.aiassistant`
   - Language: **Java**
   - Min SDK: **API 23 (Android 6.0)**
4. Click Finish

### Step 3 — Copy the Source Files
Replace the auto-generated files with the ones provided:
- Copy all `.java` files into `app/src/main/java/com/aiassistant/`
- Copy all `res/` files into `app/src/main/res/`
- Replace `AndroidManifest.xml`
- Replace both `build.gradle` files

### Step 4 — Get Your FREE Groq API Key
1. Go to https://console.groq.com
2. Sign up (free, no credit card needed)
3. Click **"Create API Key"**
4. Copy the key (starts with `gsk_...`)
5. Open `GroqApiClient.java`
6. Replace `YOUR_GROQ_API_KEY_HERE` with your key:
   ```java
   private static final String API_KEY = "gsk_xxxxxxxxxxxxxxxxxxxxxxxx";
   ```

### Step 5 — Build & Install
**Option A: Via USB (recommended)**
1. Enable Developer Options on your Huawei Y3:
   - Settings → About Phone → tap "Build Number" 7 times
   - Settings → Developer Options → USB Debugging ON
2. Connect phone via USB cable
3. In Android Studio → click the green ▶ Run button
4. Select your phone → OK

**Option B: Build APK manually**
1. Build → Generate Signed APK → Debug
2. Copy the `.apk` file to your phone
3. Enable "Unknown sources" in Settings → Security
4. Install the APK

---

## 🎙️ Voice Commands (Offline — No Internet Needed)

| Say this...              | What happens              |
|--------------------------|---------------------------|
| "Turn on flashlight"     | 🔦 Flashlight ON           |
| "Turn off flashlight"    | 🔦 Flashlight OFF          |
| "WiFi on"                | 📶 WiFi enabled            |
| "WiFi off"               | 📶 WiFi disabled           |
| "Open camera"            | 📷 Camera app opens        |
| "Open settings"          | ⚙️ Settings opens          |
| "Open calculator"        | 🧮 Calculator opens        |
| "Open WhatsApp"          | 💬 WhatsApp opens          |
| "Open YouTube"           | ▶️ YouTube in browser      |
| "Search for weather"     | 🔍 Google search opens     |
| "Open maps"              | 🗺️ Google Maps opens       |

## 🧠 AI Questions (Requires Internet)

Any question not matching a device command goes to Groq AI:
- "What is photosynthesis?"
- "Translate hello to French"
- "Write a short poem"
- "How do I make rice?"
- "What's the capital of Japan?"

---

## 🔧 Groq API — Free Tier Limits
| Limit               | Amount                    |
|---------------------|---------------------------|
| Requests/day        | 6,000                     |
| Tokens/day          | 500,000                   |
| Requests/minute     | 30                        |
| Cost                | **FREE** ✅               |

Model used: `llama3-8b-8192` — fast (usually <1 second) and smart.

---

## ⚡ Performance Tips for Low-End Phone

The app is already optimized for the Huawei Y3:
- ✅ Max 400 tokens per AI response (keeps replies short + fast)
- ✅ Only last 10 messages kept in memory
- ✅ OkHttp 3.x used (Java 7 compatible, small footprint)
- ✅ No heavy image loading
- ✅ RecyclerView (efficient, not ListView)
- ✅ minifyEnabled in release build (smaller APK)

---

## 🔧 Customization

### Change the AI personality
In `GroqApiClient.java`, edit the `systemPrompt` string:
```java
systemPrompt = "You are a funny pirate assistant. Always respond like a pirate.";
```

### Add more commands
In `CommandEngine.java`, add to the `handleCommand()` method:
```java
if (matches(cmd, "play music", "music")) {
    return openAppByName("com.spotify.music", "Spotify");
}
```

### Change AI model
In `GroqApiClient.java`:
```java
private static final String MODEL = "llama3-70b-8192"; // Smarter but slightly slower
// Other free options:
// "mixtral-8x7b-32768"  — longer context
// "gemma-7b-it"         — Google's model
```

---

## ❓ Troubleshooting

**"API key invalid"** → Double-check you copied the full key from console.groq.com

**Voice doesn't work** → Make sure Google app is installed & updated. The Y3 needs Google's speech service.

**App crashes on startup** → Check that all 5 Java files are in the same package folder.

**WiFi toggle doesn't work** → On Android 10+, WiFi can't be toggled programmatically. The app will open WiFi settings instead.

**"Thinking..." never goes away** → No internet connection, or Groq API is down. Check your data/WiFi.
