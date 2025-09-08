## DayMate – Final Report (Placeholder)

This document is a high-level placeholder to support reporting, demos, and onboarding the next developer. Each section will be expanded in the next iteration.

### Project Snapshot
- My Daily Schedule, Medication Reminder, Meal Record: Implemented and persist data to local Room database. The AI Assistant can read these data and converse with the user based on them.
- In-App Notification (Schedule/Medication): Not finished. Because the target device is temi robot (no standard Android notification system), a custom in-app full-screen notification system is required.
- AI Assistant: Uses Gemini API.
- Exercise Coach: Uses Google ML Kit.
- Social: Can add contacts.
- Memory Game: Implemented. New image assets can be added; card dataset is configurable in code.
- My Memories, Sleep Tracking, Settings/Caregiver: Not implemented yet.
- Face Age Detection: Incomplete. Currently uses Google ML Kit Face Detection only. Age estimation model (TFLite) is TBD; alternative approaches are recommended.

### Data and Storage
- Local database: Room (`AppDatabase`). Entities include:
  - `ScheduleEntity`
  - `MedicationEntity`
  - `MealRecord`
  - `MemoryEntity` (for My Memories grid; early version)
- Repositories provide access to DAOs, and ViewModels expose `Flow/StateFlow` to composables.

### In-App Notification (temi robot constraint)
- Constraint: temi robot environment cannot rely on standard Android notification services (channels/AlarmManager UI-toasts).
- Approach: Full-screen in-app overlay (Compose) triggered by:
  - Time polling (every few seconds)
  - Manual triggers (for demos)
- State and persistence:
  - Snooze/Shown-today state tracked in SharedPreferences.
  - Time normalization to handle formats like `8:00` vs `08:00`.
- Files and key components (root `app` module):
  - `InAppReminderManager.kt`: Host overlay + polling + snooze/mark-done.
  - `MainActivity.kt`: Places `InAppReminderHost(...)` above `NavHost`.
- Status: Basic overlay UI and timing logic exist; finalize reliability, UX polish, and sound policy for temi.

### AI Assistant (Gemini API)
- Where it is wired: `MainActivity.kt` constructs `GeminiAIService(apiKey = "...")`.
- Quick way: Replace the hard-coded API key with your key directly in `MainActivity.kt`.
- Recommended secure way:
  1) Put `GEMINI_API_KEY=YOUR_KEY` into your `local.properties` (not checked-in).
  2) In `app/build.gradle.kts`, add (inside `defaultConfig` or `buildTypes`):
     - `buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")`
  3) Use `BuildConfig.GEMINI_API_KEY` in `MainActivity.kt` when creating `GeminiAIService`.

### Exercise Coach (Google ML Kit)
- Uses ML Kit Pose/Face detection dependencies declared in `app/build.gradle.kts`.
- Relevant screens:
  - `ExerciseCoachScreen.kt`
  - `PoseCameraScreen.kt`
  - `PoseDemoScreen.kt`

### Social Module (Contacts)
- Screen: `SocializeScreen.kt`.
- Allows adding contacts via simple UI; future work: persistence and call/SMS integration.

### Memory Game Module – How to add assets and cards
- Place images under: `app/src/main/res/drawable/`.
  - Example existing files: `apple.jpg`, `banana.jpg`, `book.jpg`, `bus.jpg`, `car.jpg`, `cat.jpg`, `flower.jpg`, `pen.jpg`, `phone.jpg`.
- Add new card data in `MemoryGameScreen.kt`:
  - Edit `availableImages` (Triple of `R.drawable.xyz`, code-name, display-name) to include your new images.
  - The game randomly selects a subset; adjust `initializeGame()` if you need different counts or rules.

### Modules Not Implemented Yet
- My Memories: Basic grid exists (`MyMemoriesScreen`) with local URI input, but full workflow and UX are not finalized.
- Sleep Tracking: Placeholder only.
- Settings/Caregiver: Placeholder only.

### Face Age Detection – Current Status and Recommendation
- Current: Uses Google ML Kit Face Detection only (no age estimation).
- Challenge: A high-quality, on-device TFLite age model suitable for temi may be hard to source and tune.
- Recommendation:
  - Consider cloud-based age estimation if online; or
  - Use a lightweight alternative (heuristics or simpler classifier) until a robust model is available; or
  - Integrate a vetted, small TFLite model with acceptable accuracy/latency (requires evaluation and benchmarking on temi hardware).

### Build/Run Notes
- The actively compiled module is the root `app` module.
- Another module folder exists (`Temi-DayMate/app`) for reference; ensure you edit and run the root `app` unless intentionally switching.

### Next Steps Checklist
- Finalize in-app notification reliability and UX (Schedule/Medication): edge cases, snooze, persistence, sound.
- Secure Gemini API key via `BuildConfig` as described.
- Social: Persist contacts and integrate call intents/permissions.
- My Memories: Add image picker (SAF/Photo Picker), detail view, TTS narration, and better storage flow.
- Sleep Tracking: Implement basic logging and summaries.
- Face Age Detection: Choose and integrate an age estimation approach.


