## My Schedule – Development Notes

This document explains how I implemented the My Schedule module end-to-end: data model, storage, UI, in-app reminder host, and AI integration. I also list the exact file paths so the next developer can navigate quickly.

### What it does (current scope)
- View today’s schedule items
- Add a schedule (title + time)
- Mark as completed or delete
- Persist to Room database
- Allow AI Assistant to read schedules and chat using them

### Source map (root `app` module)
- Data layer
  - Entity: `app/src/main/java/com/example/myapplication/ScheduleEntity.kt`
  - DAO: `app/src/main/java/com/example/myapplication/ScheduleDao.kt`
  - Repository: `app/src/main/java/com/example/myapplication/ScheduleRepository.kt`
- UI + ViewModel
  - ViewModel: `app/src/main/java/com/example/myapplication/MyScheduleViewModel.kt`
  - Screen + Card UI: `app/src/main/java/com/example/myapplication/MainActivity.kt` → `MyScheduleScreen()` and `ScheduleCard()`
- Database
  - Room DB: `app/src/main/java/com/example/myapplication/data/AppDatabase.kt`
- In-app reminder (overlay host)
  - `app/src/main/java/com/example/myapplication/InAppReminderManager.kt`
- AI integration
  - `app/src/main/java/com/example/myapplication/ai/` (Gemini wiring + data providers)

### Data model (Room)
- `ScheduleEntity` fields: `id` (auto PK), `title` (String), `time` (String, HH:mm), `status` (PENDING/DONE), `icon`, `note`, `isDone` (bool; `status` is the source of truth)
- DAO: reactive `getAll(): Flow<List<ScheduleEntity>>` (ordered by time) + `insert/update/delete`
- Repository: thin wrapper around DAO for UI/ViewModel
- DB: `AppDatabase` registers `ScheduleEntity` and supplies `scheduleDao()`; migrations live here

### ViewModel and state
- `MyScheduleViewModel` exposes `schedules: StateFlow<List<ScheduleEntity>>` by `repo.getAll().stateIn(...)`
- Actions: `addSchedule`, `removeSchedule`, `markDone` (run on `viewModelScope`)

### UI (Compose)
- `MyScheduleScreen()` renders the page title, a `LazyColumn` list, and a FAB “+”
- Add flow: FAB → `AlertDialog` → user inputs Title/Time → `vm.addSchedule(ScheduleEntity(...))`
- Row actions: `ScheduleCard` provides Done/Delete callbacks to ViewModel

### In-app reminder overlay (temi constraint)
- Temi cannot depend on Android’s notification system
- `InAppReminderHost` sits above `NavHost` and checks due items every few seconds
- Time normalization `normalizeHHmm` lets "8:00" equal "08:00"
- Snooze/"shown today" states via `SharedPreferences`
- Manual triggers exist for demo buttons in `MyScheduleScreen`
- Status: working overlay; production-grade timing/sound UX may need polishing

### AI Assistant integration (Gemini)
- Constructed in `MainActivity.kt` and provided with repositories
- Replace the API key directly or via `BuildConfig.GEMINI_API_KEY` (recommended: define `GEMINI_API_KEY` in `local.properties` and inject with `buildConfigField`)

### Storage and migrations
- DB name: `app-db`
- Migrations in `AppDatabase.kt`; bump version and add SQL when schema changes

### Quick acceptance test
1) Run app (root `app` module) → My Schedule
2) Add an item (e.g., Title: Test, Time: 08:01) → should appear instantly
3) Use left-bottom "Test Schedule Reminder" to demo overlay
4) When current time matches item time (and not snoozed/shown), overlay should pop up
5) Press "Completed" → `status` becomes `DONE`

### Common pitfalls
- Use time format HH:mm. Bad input is sanitized to 00:00
- Overlay polling requires the app in foreground for demos
- If overlay doesn’t reappear, it may be marked "shown today" or snoozed
- For inserts, use `id = 0` so Room auto-generates PK

### Future work
- Calendar/range views; recurring items; categories/labels
- Voice input to add schedules
- Persist audit logs for snooze/confirmation
- Improve overlay accuracy and sound behavior on temi


