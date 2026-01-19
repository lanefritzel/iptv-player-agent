# FrequentSee TV Agent - TODO

## Pending Features

- [ ] Make `/cast` automatically stop existing playback before starting new stream
  - When a new cast request comes in, broadcast stop intent first
  - Ensures seamless stream switching without needing to call `/stop` manually

- [ ] Notify originating app when playback stops
  - When user backs out of stream (back button)
  - When Chromecast shuts down
  - Need to figure out callback mechanism (webhook URL in cast request?)

## Completed

- [x] Fix network IP detection (use NetworkInterface instead of WifiManager)
- [x] Enable cleartext HTTP traffic for local streams
- [x] Auto-start service on device boot
