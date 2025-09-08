## My Memories – Development Notes

This document explains the current state of the My Memories module and how to extend it.

### What it does (current scope)
- Basic grid screen exists; allows adding a photo by URI (for demo purposes)

### Source map (root `app` module)
- Data layer
  - Entity: `app/src/main/java/com/example/myapplication/MemoryEntity.kt`
  - DAO: `app/src/main/java/com/example/myapplication/MemoryDao.kt`
  - Repository: `app/src/main/java/com/example/myapplication/MemoryRepository.kt`
  - ViewModel: `app/src/main/java/com/example/myapplication/MemoryViewModel.kt`
- UI
  - Screen: `app/src/main/java/com/example/myapplication/MemoryScreen.kt` (`MyMemoriesScreen`)
- Database
  - Room DB and migration `MIGRATION_4_5` in `app/src/main/java/com/example/myapplication/data/AppDatabase.kt`

### Future work
- Add a Photo Picker (SAF/Photo Picker) with persisted URI permissions
- Detail view with zoom and TTS narration of title/description
- Albums/tags and caregiver sharing


