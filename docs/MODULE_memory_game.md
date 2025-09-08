## Memory Game – Development Notes

This document explains the Memory Game module and how to add assets/cards.

### What it does (current scope)
- Simple image-based memory game with preview → playing → complete states
- Sound feedback for correct/wrong selections

### Source map (root `app` module)
- Screen: `app/src/main/java/com/example/myapplication/MemoryGameScreen.kt`
- Assets: `app/src/main/res/drawable/` (e.g., `apple.jpg`, `banana.jpg`, `book.jpg`, ...)

### Adding images/cards
- Place new images into `app/src/main/res/drawable/`
- Open `MemoryGameScreen.kt` and find `availableImages`
- Add entries like `Triple(R.drawable.your_image, "your_code_name", "Your Display Name")`
- The game randomly selects a subset; adjust counts/logic in `initializeGame()` if needed

### Future work
- More game modes (pair matching, timed challenges)
- Difficulty scaling
- Leaderboard or caregiver share


