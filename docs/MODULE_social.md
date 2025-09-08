## Social – Development Notes

This document explains the Social module’s current state and how to extend it.

### What it does (current scope)
- Provide a screen to add/display contacts via large buttons (friendly for the target users)

### Source map (root `app` module)
- Screen: `app/src/main/java/com/example/myapplication/SocializeScreen.kt`

### Current implementation
- UI shows placeholder-friendly layout and basic actions
- No persistent contacts storage yet

### Future work
- Persist contacts (Room entity + DAO + repository)
- Add call intents (ACTION_CALL) with proper permissions
- Optional: categories (family, caregiver), speed-dial, photo syncing


