# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FrequentSee TV Agent** is an Android TV cast receiver application that receives and plays HLS streams from a companion web app over HTTP. It bypasses Chromecast's HTTPS requirement by running a local HTTP server on the TV device.

- **Package**: `com.frequentsee.tv.agent`
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 36
- **Build System**: Gradle with Kotlin DSL
- **Server Port**: 8080 (HTTP)

## Development Commands

### Build and Run
```bash
./gradlew build                    # Build the entire project
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK
./gradlew installDebug             # Install debug APK to connected device
./gradlew clean                    # Clean build artifacts
```

### Testing
```bash
./gradlew test                     # Run unit tests
./gradlew connectedAndroidTest     # Run instrumented tests on device/emulator
./gradlew app:testDebugUnitTest    # Run unit tests for app module
```

### Code Quality
```bash
./gradlew lint                     # Run Android lint checks
./gradlew lintDebug                # Lint debug build variant
```

### Dependencies
```bash
./gradlew dependencies             # Show dependency tree
./gradlew app:dependencies         # Show app module dependencies
```

## Architecture

### Technology Stack
- **UI Framework**: Jetpack Compose for TV (androidx.tv.material3)
- **Video Player**: ExoPlayer (Media3) for HLS playback
- **HTTP Server**: NanoHTTPD (embedded, lightweight)
- **JSON**: Gson for request/response serialization
- **Language**: Kotlin 2.0.21 with Compose compiler plugin
- **Build Tool**: Android Gradle Plugin 9.0.0

### Key Components

1. **MainActivity** (`MainActivity.kt`)
   - Launcher activity displaying receiver status
   - Shows cast endpoint URL (http://[TV_IP]:8080/cast)
   - Starts ReceiverService as foreground service
   - Includes test button for sample HLS playback

2. **ReceiverService** (`ReceiverService.kt`)
   - Foreground service running the HTTP server
   - Keeps server alive when activity is paused
   - Shows persistent notification with endpoint URL
   - Automatically retrieves and displays device IP address

3. **CastServer** (`server/CastServer.kt`)
   - NanoHTTPD server listening on port 8080
   - Endpoints: POST /cast, POST /stop, GET /status
   - Handles CORS for web app compatibility
   - Broadcasts stop intent to PlayerActivity

4. **PlayerActivity** (`PlayerActivity.kt`)
   - Fullscreen video player using ExoPlayer
   - Receives stream URL, title, subtitle via Intent
   - Shows overlay UI for 5 seconds, then auto-hides
   - Listens for stop broadcast and back button events
   - Releases player resources on destroy

5. **Models** (`models/Models.kt`)
   - `CastRequest` - Incoming cast request data
   - `PlaybackState` - Current playback state
   - `ApiResponse` - Standard API response format

### Data Flow

```
Web App → HTTP POST /cast → CastServer
                               ↓
                    Creates Intent with stream URL
                               ↓
                    Starts PlayerActivity
                               ↓
                    ExoPlayer plays HLS stream
                               ↓
                    Shows overlay UI (5s auto-hide)
```

### Key Design Considerations
- **HTTP over HTTPS**: Works with plain HTTP (no SSL required)
- **Foreground service**: Survives activity pauses and background state
- **TV-optimized**: Uses TV Material3 components, handles TV remote input
- **Minimal UI**: Fullscreen video with brief overlay, no persistent controls
- **Graceful cleanup**: Properly releases ExoPlayer and unregisters broadcast receivers
- **CORS enabled**: Web apps can make cross-origin requests

## Important Notes

### Dependencies
- **ExoPlayer (Media3)**: Version 1.5.0 - Used for HLS playback
- **NanoHTTPD**: Version 2.3.1 - Lightweight HTTP server
- **Gson**: Version 2.10.1 - JSON serialization
- **TV Material3**: 1.0.0-alpha07 (alpha, expect API changes)

### Permissions
The app requires these permissions:
- `INTERNET` - Network communication
- `FOREGROUND_SERVICE` - Keep server running in background
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Media playback service type
- `POST_NOTIFICATIONS` - Show foreground service notification

### Build Configuration
- Java 11 required (sourceCompatibility/targetCompatibility = VERSION_11)
- ProGuard disabled in release builds (isMinifyEnabled = false)
- Uses Compose BOM for version management
- ViewBinding not required (using AndroidView for PlayerView)

### Testing
- Use "Test Playback" button in MainActivity for quick testing
- Sample HLS stream: Apple's bipbop example
- For API testing: `curl -X POST http://[TV_IP]:8080/cast -H "Content-Type: application/json" -d '{"streamUrl":"...","title":"...","subtitle":"..."}'`

### Known Limitations
- HTTP only (no HTTPS support yet)
- No pause/resume controls (coming in Phase 3)
- No seek functionality (coming in Phase 3)
- mDNS discovery not yet implemented (Phase 2)
- Single stream at a time (new cast request stops current playback)
