# Keep OkHttp (used for Groq API calls)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep our app classes
-keep class com.aiassistant.** { *; }

# Keep JSON parsing
-keep class org.json.** { *; }

# Keep Android support library
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
