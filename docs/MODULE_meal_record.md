## Meal Record – Development Notes

This document explains how I implemented the Meal Record module: data model, storage, UI, and where to extend.

### What it does (current scope)
- Record meals with time, type, optional photo/mood/notes
- Persist to Room
- AI Assistant can read recent meals for conversations

### Source map (root `app` module)
- Data layer
  - Entity: `app/src/main/java/com/example/myapplication/MealRecord.kt`
  - DAO: `app/src/main/java/com/example/myapplication/MealDao.kt`
  - Repository: `app/src/main/java/com/example/myapplication/MealRepository.kt`
- UI + ViewModel
  - Screen: `app/src/main/java/com/example/myapplication/MealRecordScreen.kt`
  - Dialog: `app/src/main/java/com/example/myapplication/MealRecordDialog.kt`
- Database
  - Room DB: `app/src/main/java/com/example/myapplication/data/AppDatabase.kt`

### Data model (Room)
- `MealRecord` fields include: `id` (auto PK), `timestamp`, `mealType`, `photoPath` (nullable), `mood` (nullable), `description` (nullable), `waterIntake` (int), `isCompleted` (bool)
- DAO/Repository expose CRUD and flows to UI
- `MIGRATION_3_4` created `meal_records` table

### UI (Compose)
- `MealRecordScreen` lists meals and opens `MealRecordDialog` to add/edit
- `MealRecordDialog` supports image picking (placeholder), mood, and notes
- Data flows back to ViewModel/Repository → Room

### AI Assistant integration (Gemini)
- Same pattern: Gemini service can read meal data via repository for context-aware conversations

### Storage and migrations
- DB: `app-db`
- Migrations in `AppDatabase.kt` (notably `MIGRATION_3_4`)

### Quick acceptance test
1) Run app → Meal Record
2) Add a record (meal type, optional notes)
3) Verify it persists and shows in the list

### Common pitfalls
- Photo URIs/permissions: add SAF/Photo Picker in future if needed
- Keep timestamps in millis; convert in UI as needed

### Future work
- Real camera/gallery picker with persisted URI permissions
- Charts/summary (meal balance, hydration)
- Caregiver export/share


