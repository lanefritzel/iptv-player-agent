# FrequentSee TV Agent

An Android TV application that receives and plays HLS streams from a companion web app over HTTP on a local network, bypassing Chromecast's HTTPS requirement.

## Architecture

```
Web App (phone/tablet browser) → HTTP → Android TV App → ExoPlayer
     ↓                                        ↓
  "Cast" button                         Plays HLS stream
  mDNS discovery                        Shows channel info
```

## Features

### Phase 1: Basic Receiver ✅
- **HTTP Server** - Listens on port 8080 for incoming cast requests
- **ExoPlayer Integration** - Plays HLS streams fullscreen
- **Overlay UI** - Shows title/subtitle briefly (5 seconds), then hides
- **Transport Controls** - Stop playback via API or TV remote back button

### Phase 2: mDNS Discovery (Coming Soon)
- Register mDNS service (`_frequentsee._tcp`) on local network
- Web app discovers service and shows "Cast to [TV Name]" button

### Phase 3: Enhanced Controls (Coming Soon)
- Pause/resume functionality
- Seek controls
- Volume control

## API Endpoints

The app exposes the following HTTP endpoints on port 8080:

### POST /cast
Start playback of an HLS stream.

**Request Body:**
```json
{
  "streamUrl": "http://192.168.x.x:5000/api/stream/ch123/playlist.m3u8",
  "title": "Channel Name",
  "subtitle": "Program Title"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Playback started"
}
```

### POST /stop
Stop current playback.

**Response:**
```json
{
  "success": true,
  "message": "Playback stopped"
}
```

### GET /status
Get current playback status.

**Response:**
```json
{
  "status": "playing",
  "streamUrl": "http://...",
  "title": "Channel Name",
  "subtitle": "Program Title"
}
```

Status values: `idle`, `playing`, `stopped`

## Building and Running

### Prerequisites
- Android Studio with Android SDK
- Java 11 or higher
- Android TV device or emulator

### Build Commands
```bash
./gradlew build                    # Build the project
./gradlew assembleDebug            # Build debug APK
./gradlew installDebug             # Install to connected device
```

### Testing
1. Install the app on your Android TV device
2. Launch the app - it will display the cast endpoint URL
3. Click the "Test Playback" button to test with a sample HLS stream
4. Use the back button on your TV remote to stop playback

### Testing from Web App
From your web app or any HTTP client, send a POST request:

```bash
curl -X POST http://<TV_IP_ADDRESS>:8080/cast \
  -H "Content-Type: application/json" \
  -d '{
    "streamUrl": "http://192.168.1.100:5000/api/stream/ch123/playlist.m3u8",
    "title": "My Channel",
    "subtitle": "Current Program"
  }'
```

## Project Structure

```
app/src/main/java/com/frequentsee/tv/agent/
├── MainActivity.kt              # Main UI showing receiver status
├── PlayerActivity.kt            # Fullscreen video player with overlay
├── ReceiverService.kt          # Foreground service running HTTP server
├── models/
│   └── Models.kt               # Data classes (CastRequest, PlaybackState)
├── server/
│   └── CastServer.kt           # NanoHTTPD server handling /cast, /stop, /status
└── ui/theme/                   # Compose theme files
```

## Technologies Used

- **Kotlin** - Programming language
- **Jetpack Compose for TV** - UI framework
- **ExoPlayer (Media3)** - HLS playback engine
- **NanoHTTPD** - Embedded HTTP server
- **Gson** - JSON serialization

## Permissions

The app requires the following permissions:
- `INTERNET` - Network communication
- `FOREGROUND_SERVICE` - Keep HTTP server running
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Media playback service type
- `POST_NOTIFICATIONS` - Show foreground service notification

## Notes

- The app works over plain HTTP (no HTTPS required)
- Runs as a foreground service to survive activity pauses
- Handles network changes gracefully
- Minimal UI - fullscreen video with brief overlay
- TV remote compatible (D-pad navigation, back to stop)

## Future Enhancements

- [ ] mDNS service registration for automatic discovery
- [ ] Pause/resume controls
- [ ] Seek functionality
- [ ] Volume control
- [ ] Playlist support
- [ ] Picture-in-picture mode
- [ ] Playback history
- [ ] Custom notification with playback controls
