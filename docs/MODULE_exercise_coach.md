## Exercise Coach – Development Notes

This document explains how the Exercise Coach features are structured and where ML Kit is used.

### What it does (current scope)
- Demo gentle exercise flows and camera-based pose/face detection

### Source map (root `app` module)
- Screens:
  - `app/src/main/java/com/example/myapplication/ExerciseCoachScreen.kt`
  - `app/src/main/java/com/example/myapplication/PoseCameraScreen.kt`
  - `app/src/main/java/com/example/myapplication/PoseDemoScreen.kt`
- ML Kit dependencies: declared in `app/build.gradle.kts`

### Implementation notes
- Camera preview and overlays in `PoseCameraScreen`
- Demo drawing/logic in `PoseDemoScreen`
- Top-level navigation and UI scaffolding in `ExerciseCoachScreen`

### Future work
- Integrate ML Kit Pose estimation pipeline end-to-end (reps counting, form hints)
- Performance tuning on temi hardware
- Safety prompts and caregiver alerts


