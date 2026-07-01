# Telegram ULTRA Backup

> Auto-backup Telegram media to any Telegram chat via a Bot — runs silently in the background.

## Features

- **Auto-detection** — watches Telegram media folders in real-time using FileObserver
- **Deduplication** — SHA-256 hashing prevents re-uploading the same file
- **Retry logic** — exponential backoff via WorkManager if upload fails
- **Encrypted storage** — Bot Token and Chat ID stored with AES-256 encryption
- **Network-aware** — only uploads when connected to the internet
- **Boot persistence** — automatically restarts after device reboot
- **Multilingual** — English + Arabic (RTL supported)
- **Room database** — tracks all uploaded files with history

## Setup

1. Create a Telegram bot via [@BotFather](https://t.me/BotFather) and copy the **Bot Token**
2. Get your **Chat ID** (send a message to your bot, then open `https://api.telegram.org/bot{TOKEN}/getUpdates`)
3. Open the app → tap **Configure** → enter your Token and Chat ID → tap **Test**
4. Enable the service toggle

## Architecture

```
UltraBackupEngine
├── MediaScanner      → FileObserver on Telegram directories
├── EventQueue        → Deduplication buffer
└── UploadWorker      → WorkManager + TelegramClient (OkHttp)

AppDatabase (Room)    → FileRecord (hash, path, size, status)
PrefsManager          → EncryptedSharedPreferences (token, chatId)
BackupService         → Foreground service (START_STICKY)
BootReceiver          → Auto-start on device boot
```

## Monitored Paths

- `/storage/emulated/0/Telegram/`
- `/storage/emulated/0/Android/media/org.telegram.messenger/`
- `/storage/emulated/0/Android/media/org.telegram.messenger.web/`
- `/storage/emulated/0/Android/media/org.telegram.messenger.beta/`

## Requirements

- Android 8.0+ (API 26+)
- Telegram account + Bot token

## Build

```bash
./gradlew assembleDebug
```

## License

MIT
