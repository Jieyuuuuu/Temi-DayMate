## Face Age Detection – Development Notes

This document explains the current status and the recommended path forward.

### What it does (current scope)
- Uses Google ML Kit Face Detection (no age estimation yet)

### Source map (root `app` module)
- Screen: `app/src/main/java/com/example/myapplication/FaceAgeDetectionScreen.kt`
- ML Kit dependencies: declared in `app/build.gradle.kts`

### Constraints and recommendations
- A robust, on-device TFLite age model suitable for temi may be hard to source and tune
- Options:
  1) Cloud inference (if online); or
  2) Lightweight local heuristic/classifier as a stopgap; or
  3) Integrate a vetted small TFLite age model and benchmark on temi hardware

### Future work
- Choose an age estimation approach and integrate
- Calibrate performance and accuracy on-device
- Add privacy prompts and user consent flows


