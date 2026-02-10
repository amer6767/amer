# Echo Gardens - Walkthrough 🌿

A living digital ecosystem Android game that grows with your focus.

## What Was Built

| Component | Description |
|-----------|-------------|
| **Aura Engine** | Bloom/Mist phases based on focus consistency |
| **Focus Sessions** | Timer with category selection (Work/Health/Learning/Social) |
| **World Tree** | Animated tree with branches that grow per category |
| **Island Canvas** | Beautiful 2D rendering with phase-based colors |
| **Haptic Feedback** | Heartbeat vibration on session completion |
| **Stardust System** | Reward currency earned from focus sessions |

---

## Project Structure

```
app/
├── src/main/
│   ├── java/com/echogardens/app/
│   │   ├── MainActivity.kt          # App entry, navigation
│   │   ├── engine/
│   │   │   ├── AuraEngine.kt         # Core game logic + persistence
│   │   │   └── IslandState.kt        # Data models
│   │   ├── focus/
│   │   │   └── FocusViewModel.kt     # Timer management
│   │   ├── audio/
│   │   │   ├── HapticManager.kt      # Vibration patterns
│   │   │   └── SoundManager.kt       # Battery-based audio
│   │   └── ui/
│   │       ├── theme/                # Compose theming
│   │       ├── components/
│   │       │   └── IslandCanvas.kt   # World Tree rendering
│   │       └── screens/              # Home, Focus, Settings
│   └── res/                          # Resources, icons
├── build.gradle.kts                  # Dependencies
└── proguard-rules.pro
```

---

## How to Build & Run

### In Android Studio:

1. **Open project**: `File → Open → c:\Users\Lenovo\Desktop\amer`
2. **Wait for Gradle sync** (may take 1-2 minutes first time)
3. **Connect your phone** via USB with Developer Mode enabled
4. **Click Run** ▶️ or press `Shift+F10`

### Via Command Line:
```bash
cd c:\Users\Lenovo\Desktop\amer
.\gradlew assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## Testing Flow

1. **Launch app** → See island in **Mist Phase** (blue-gray, foggy)
2. **Tap "Start Focus"** → Choose **Work** category → Select **1 min**
3. **Tap "Begin Focus"** → Watch breathing timer animation
4. **Wait for completion** → Feel haptic vibration → See stardust reward dialog
5. **Return to home** → Island now in **Bloom Phase** (vibrant colors!)
6. **Notice World Tree** → Work branch glows brighter after session

---

## Features Implemented

- ✅ Focus Sessions with timer
- ✅ Category tracking (Work/Health/Learning/Social)
- ✅ Bloom/Mist phase transitions
- ✅ World Tree with animated branches
- ✅ Stardust rewards
- ✅ Haptic feedback
- ✅ Settings (haptics/sound toggles)
- ✅ DataStore persistence

## Future Phases (Not in MVP)

- ❌ Ghost Guardians (social)
- ❌ Melody Stones (audio collaboration)
- ❌ The Great Constellation (multiplayer)
- ❌ Night Market (trading)
