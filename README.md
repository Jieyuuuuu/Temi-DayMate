## DayMate (temi robot Android)

High-level overview only. For details or troubleshooting, please read the module docs in `docs/` and `homepage.md`.

### What this app is
- A companion app (Jetpack Compose + Room) designed for temi robot.
- Modules include: My Schedule, Medication Reminder, Meal Record, Social, Exercise Coach, Memory Game, etc.

### Deploy to temi (ADB over network)
1) Download Android SDK Platform Tools from Google (adb)
2) Open a terminal:
   - `cd platform-tools`
   - `adb connect 10.49.131.194`
     - If it fails, check the temi robot Settings → “ADB connection to temi” to confirm the correct IP.
     - Make sure your computer and the temi robot are on the same network.
     - temi robot cannot join typical eduroam; request a whitelist or use an IoT network.
3) Build with Android Studio. When ADB is connected, pressing Run will deploy directly to the temi robot.

### Gemini API key (do not hardcode)
- Preferred setup:
  1) Put `GEMINI_API_KEY=YOUR_KEY` into `local.properties` (not committed)
  2) In `app/build.gradle.kts`, add:
     - `buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")`
  3) Use `BuildConfig.GEMINI_API_KEY` when constructing the Gemini service.

Note: A previously hardcoded key has been removed. Follow the steps above to inject your key safely.

### Next steps
- See `docs/` for per-module guides and `homepage.md` for a broader introduction.


