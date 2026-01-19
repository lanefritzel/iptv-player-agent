# FrequentSee TV Agent

An Android TV application that receives and plays HLS/IPTV streams from a companion web app over your local network.

## What It Does

This app turns your Chromecast with Google TV (or any Android TV device) into a cast receiver for IPTV streams. Instead of using Chromecast's built-in casting (which requires HTTPS), this app runs a local HTTP server that your web app can send streams to directly.

```
┌─────────────────┐         HTTP POST          ┌─────────────────┐
│   Web Browser   │  ──────────────────────►   │   Android TV    │
│  (Phone/Laptop) │     /cast endpoint         │   FrequentSee   │
│                 │                            │      Agent      │
│  "Cast" button  │  ◄──────────────────────   │                 │
└─────────────────┘      Stream plays          └─────────────────┘
```

---

## Installation Guide

### Prerequisites

- Chromecast with Google TV, or any Android TV device
- The TV and your phone/computer must be on the **same WiFi network**
- A way to install the APK (see options below)

### Option 1: Install via ADB (Recommended)

1. **Enable Developer Options on your Android TV:**
   - Go to Settings → Device Preferences → About
   - Click on "Build" 7 times to enable Developer Options
   - Go back to Device Preferences → Developer Options
   - Enable "USB debugging" (or "Network debugging")

2. **Connect via ADB:**
   ```bash
   # Find your TV's IP address (shown in Settings → Network & Internet)
   adb connect <TV_IP_ADDRESS>:5555

   # Install the APK
   adb install app-debug.apk
   ```

3. **Launch the app** from your TV's app list

### Option 2: Install via USB Drive

1. Copy the APK to a USB drive
2. Plug the USB drive into your Android TV
3. Use a file manager app to locate and install the APK
4. You may need to enable "Install from unknown sources"

---

## Using the App

### First Launch

1. Open **FrequentSee TV Agent** on your Android TV
2. The app will display:
   - "Ready to receive streams"
   - The cast endpoint URL: `http://<YOUR_TV_IP>:8080/cast`
3. **Note this IP address** - you'll need it for your web app

### Receiving Streams

Once the app is running, it listens for incoming stream requests. Your companion web app sends a request to the TV's endpoint, and the stream plays automatically.

### Stopping Playback

- Press the **Back** button on your TV remote
- Or send a stop request from your web app

### Auto-Start on Boot

After the first launch, the app will automatically start in the background whenever your TV boots up. You don't need to manually open it each time.

---

## Network Requirements

### Same Network Required

Both your streaming device (phone/laptop) and the Android TV must be on the **same local network** and able to communicate with each other.

### Common Issues

| Problem | Cause | Solution |
|---------|-------|----------|
| Can't connect to TV | Devices on different networks | Ensure both are on same WiFi |
| Connection refused | AP isolation enabled | Disable "AP Isolation" in router settings |
| Stream won't play | Firewall blocking | Allow port 8080 on your network |
| "Connecting to network..." stuck | TV not connected to WiFi/Ethernet | Check TV's network settings |

### Port Used

The app listens on **port 8080**. Ensure this port isn't blocked by your router or firewall.

---

## For Web App Developers

### API Endpoints

The app exposes the following HTTP endpoints on port 8080:

#### POST /cast
Start playback of a stream.

```javascript
// JavaScript example
const response = await fetch(`http://${tvIp}:8080/cast`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    streamUrl: 'http://your-server/stream.m3u8',
    title: 'Channel Name',
    subtitle: 'Program Title'
  })
});
```

**Request Body:**
```json
{
  "streamUrl": "http://192.168.x.x:5000/stream/playlist.m3u8",
  "title": "Channel Name",
  "subtitle": "Program Title"
}
```

**Response:**
```json
{ "success": true, "message": "Playback started" }
```

#### POST /stop
Stop current playback.

```bash
curl -X POST http://<TV_IP>:8080/stop
```

**Response:**
```json
{ "success": true, "message": "Playback stopped" }
```

#### GET /status
Get current playback status.

```bash
curl http://<TV_IP>:8080/status
```

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

### CORS

CORS is enabled (`Access-Control-Allow-Origin: *`), so web apps can make requests directly from the browser.

### Stream Formats

The app uses ExoPlayer which supports:
- HLS (.m3u8)
- DASH (.mpd)
- Progressive MP4
- Most common streaming formats

---

## Troubleshooting

### "Connecting to network..." message won't go away

- The TV isn't connected to WiFi or Ethernet
- Check Settings → Network & Internet on your TV
- Restart the app after connecting

### Stream shows black screen with title

- The stream URL may be unreachable from the TV
- Test the stream URL works from another device on the same network
- Ensure your stream server allows connections from the TV's IP

### App doesn't start after reboot

- Open the app manually once after installing
- Android requires the app to be launched once to activate auto-start

### Can't connect from web app

1. Verify the TV's IP address (shown on the app's main screen)
2. Try pinging the TV: `ping <TV_IP>`
3. Try the status endpoint: `curl http://<TV_IP>:8080/status`
4. Check if AP isolation is disabled on your router

---

## Building from Source

### Prerequisites
- Android Studio with Android SDK
- Java 11 or higher

### Build Commands
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew installDebug       # Install to connected device
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`

---

## Technical Details

### Technologies Used
- **Kotlin** - Programming language
- **Jetpack Compose for TV** - UI framework
- **ExoPlayer (Media3)** - Video playback
- **NanoHTTPD** - Embedded HTTP server
- **Gson** - JSON parsing

### Permissions Required
- `INTERNET` - Network communication
- `FOREGROUND_SERVICE` - Keep server running in background
- `RECEIVE_BOOT_COMPLETED` - Auto-start on boot
- `POST_NOTIFICATIONS` - Show service notification

### Project Structure
```
app/src/main/java/com/frequentsee/tv/agent/
├── MainActivity.kt        # Main screen showing IP/status
├── PlayerActivity.kt      # Fullscreen video player
├── ReceiverService.kt     # Background HTTP server
├── BootReceiver.kt        # Auto-start on boot
├── models/Models.kt       # Data classes
└── server/CastServer.kt   # HTTP endpoint handlers
```

---

## License

[Add your license here]
