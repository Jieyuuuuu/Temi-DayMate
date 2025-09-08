## DayMate – Developer Home (docs index)

Welcome to DayMate (temi robot Android). This page is the starting point for reports, demos, and onboarding. It links to per‑module guides, setup steps, and known constraints.

### Quick start
1) Open the project in Android Studio
2) Select the root `app` module for run/build
3) Run on a device/emulator (for temi, deploy to the robot environment)

### Environment setup (Gemini API)
Recommended (do not hardcode keys):
- Add to `local.properties` (not checked in):
  - `GEMINI_API_KEY=YOUR_KEY`
- In `app/build.gradle.kts` add within `defaultConfig` or a `buildType`:
  - `buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")`
- Use `BuildConfig.GEMINI_API_KEY` when constructing the Gemini service in `MainActivity.kt`.

### In‑app reminder (temi constraint)
- temi does not support standard Android notifications for this app context.
- A full‑screen in‑app overlay handles reminders (schedules/medications):
  - Host: `app/src/main/java/com/example/myapplication/InAppReminderManager.kt`
  - Placed above `NavHost` in `MainActivity.kt`
  - Polls every few seconds; uses SharedPreferences for snooze/"shown today"

### Project structure (high‑level)
- UI (Compose) screens live under `app/src/main/java/com/example/myapplication/*.kt`
- Data layer (Room) under `app/src/main/java/com/example/myapplication/data/` and adjacent DAOs/entities
- AI integration under `app/src/main/java/com/example/myapplication/ai/`
- Docs under `docs/`

### Module guides
- My Schedule: `docs/MODULE_my_schedule.md`
- Medication Reminder: `docs/MODULE_medication_reminder.md`
- Meal Record: `docs/MODULE_meal_record.md`
- Social: `docs/MODULE_social.md`
- Exercise Coach: `docs/MODULE_exercise_coach.md`
- Memory Game: `docs/MODULE_memory_game.md`
- My Memories: `docs/MODULE_my_memories.md`
- Sleep Tracking: `docs/MODULE_sleep_tracking.md`
- Settings/Caregiver: `docs/MODULE_settings_caregiver.md`
- Face Age Detection: `docs/MODULE_face_age_detection.md`

### Final report
- `FINAL_REPORT.md` (project snapshot, constraints, and next steps)

### Build/run notes
- Actively compiled module: root `app`
- Another module folder exists (`Temi-DayMate/app`) for reference; modify/run the root `app` unless intentionally switching

### Coding conventions
- Kotlin, Jetpack Compose, Room, coroutines/Flow
- Explicit state hoisting to ViewModels; repositories hide Room details
- Keep functions descriptive; prefer clear names over abbreviations

### Troubleshooting
- Reminders don’t show:
  - Ensure app is in foreground; check snooze/"shown today" flags
  - Confirm time format `HH:mm` and current minute alignment
- Gemini key not read:
  - Verify `local.properties` value and `build.gradle.kts` `buildConfigField`
  - Use `BuildConfig.GEMINI_API_KEY` at runtime


