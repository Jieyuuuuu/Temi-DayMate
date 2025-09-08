## Medication Reminder – Development Notes

This document explains how I implemented the Medication Reminder module: data model, storage, UI, and the current status of the in-app reminder (temi constraint). I also list exact file paths.

### What it does (current scope)
- Maintain a list of medications (name, dosage, time)
- Add/edit medications via dialog
- Persist to Room database
- AI Assistant can read medications for conversations
- In-app reminder overlay is wired in the shared host (see below); production polish pending

### Source map (root `app` module)
- Data layer
  - Entity: `app/src/main/java/com/example/myapplication/data/MedicationEntity.kt`
  - DAO: `app/src/main/java/com/example/myapplication/data/MedicationDao.kt`
  - Repository: `app/src/main/java/com/example/myapplication/data/MedicationRepository.kt`
- UI
  - Screen: `app/src/main/java/com/example/myapplication/MedicationReminderScreen.kt`
  - Dialog: `app/src/main/java/com/example/myapplication/MedicationDialog.kt`
- Database
  - Room DB: `app/src/main/java/com/example/myapplication/data/AppDatabase.kt`
- In-app reminder (shared overlay host)
  - `app/src/main/java/com/example/myapplication/InAppReminderManager.kt`
- Deprecated (system notifications)
  - `app/src/main/java/com/example/myapplication/NotificationService.kt` (kept for reference; not used on temi)

### Data model (Room)
- `MedicationEntity` fields: `id` (auto PK), `name`, `dosage`, `reminderTime` (HH:mm), `note` (nullable), `isActive` (bool), `createdAt` (epoch millis)
- DAO: typical CRUD; `getActiveMedications()` (Flow) feeds UI and reminder host
- Repository: thin abstraction over DAO, used by UI and AI
- DB: `AppDatabase` registers `MedicationEntity` and `medicationDao()`; migrations live there

### UI (Compose)
- `MedicationReminderScreen` renders list, and handles add/edit via `MedicationDialog`
- Items can be toggled active/inactive, edited, or removed
- The original AlarmManager/Notification path is removed for temi; overlay host handles reminders instead

### In-app reminder overlay (temi constraint)
- Temi cannot rely on Android notifications
- Shared host `InAppReminderHost` checks due medications (and schedules) every few seconds
- Uses SharedPreferences to prevent repeats and implement snooze
- Manual demo trigger buttons are provided in `MyScheduleScreen` for convenience
- Status: working overlay, but timing/sound UX needs final tuning

### AI Assistant integration (Gemini)
- The Gemini service (constructed in `MainActivity.kt`) can read medications through the repository
- API key injection is the same as other modules (prefer `BuildConfig.GEMINI_API_KEY`)

### Storage and migrations
- DB name: `app-db`
- Migrations in `AppDatabase.kt`; bump version and add SQL on schema changes

### Quick acceptance test
1) Run app → Medication Reminder
2) Add a medication (Name, Dosage, Time) and mark active
3) Verify it persists and re-appears on next launch
4) Set time to the next minute; when time matches (and not snoozed/shown), overlay should pop up

### Common pitfalls
- Use HH:mm 24h time
- If overlay doesn’t show: it may be snoozed or already shown for today
- On temi, system notifications won’t appear; only the overlay applies

### Future work
- Recurrence support, multiple times per day
- Adherence logging and caregiver escalation policy
- Richer reminders (photo pill, larger text, TTS)


