# Nora — 本地离线 AI 智能体

**Nora** is a **local-first, privacy-first** personal AI agent that runs entirely on your Android device. No cloud. No data collection. No internet required.

---

## Your Data Stays With You

Unlike cloud AI assistants, Nora processes everything locally using on-device LLMs. Your conversations, notifications, and files never leave your phone.

---

## Features

- **Offline Inference** — Runs [Qwen3-0.6B](https://huggingface.co/Qwen) quantized locally via ExecuTorch. Zero network requests after setup.
- **Notification Intelligence** — Aggregates and summarizes notifications from all apps using NotificationListenerService. No more notification overload.
- **File Context** — Load documents (.txt, .md, .json) into conversations via SAF (Storage Access Framework). Your files, your context.
- **Dark-Native UI** — Jetpack Compose UI with Matrix-inspired dark theme. Built for night owls.
- **Local Persistence** — Room database for conversations and context. All data stored on-device.
- **Modular Architecture** — Phase-driven development with full test coverage and Instrument tests.

## Architecture

```
+---------------------------------------------------------------------+
|                          Nora App                                     |
+---------------------------------------------------------------------+
|  UI Layer (Jetpack Compose)                                          |
|  +-- Chat Screen                                                     |
|  +-- Notification Aggregation                                        |
|  +-- Settings & Permissions                                          |
+---------------------------------------------------------------------+
|  Domain Layer (ViewModels + Use Cases)                               |
+---------------------------------------------------------------------+
|  Data Layer (Room + Repository Pattern)                              |
|  +-- ConversationDao                                                 |
|  +-- NotificationDao (Phase 6)                                       |
|  +-- FileContextDao (Phase 6)                                        |
+---------------------------------------------------------------------+
|  LLM Layer (ExecuTorch + Qwen3-0.6B)                                |
|  +-- Local inference engine, no network                              |
+---------------------------------------------------------------------+
```

## Tech Stack

| Layer        | Technology                              |
|--------------|----------------------------------------|
| Language     | Kotlin 1.9+                            |
| UI           | Jetpack Compose (Material 3)           |
| Architecture | MVVM + Clean Architecture             |
| DI           | Hilt                                   |
| Database     | Room                                   |
| LLM          | ExecuTorch + Qwen3-0.6B               |
| Navigation   | Navigation Compose                     |
| Async        | Kotlin Coroutines + Flow              |
| Background   | WorkManager                            |

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34+
- JDK 17+
- Android device or emulator (API 26+)

### Build

```bash
# Clone the repo
git clone https://github.com/HarnessTeam/Nora.git
cd Nora

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

### Model Setup

Place your quantized Qwen3 model at:
```
/data/local/tmp/llama/model.pte
```

The model file is not included in this repository due to size. Obtain it from:
- [Qwen3 Official](https://huggingface.co/Qwen)
- Or any ExecuTorch-compatible quantized format

### Run Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrument tests (requires device/emulator)
./gradlew connectedDebugAndroidTest
```

## Development

### Phase Roadmap

| Phase    | Status          | Description                         |
|----------|-----------------|-------------------------------------|
| Phase 0  | Complete        | Foundation (package, theming, CI)  |
| Phase 1  | In Progress     | Data persistence (Room, Repository) |
| Phase 2  | Pending         | Navigation & UI structure          |
| Phase 3  | Pending         | LLM integration                     |
| Phase 4  | Pending         | Chat UI                            |
| Phase 5  | Pending         | Settings & permissions             |
| Phase 6  | Pending         | Notification aggregation + summary |
| Phase 7  | Pending         | File context injection              |

### Constitution

Nora follows a strict **Constitution** for privacy and security:

- **No INTERNET permission** - Zero network capability
- **No data exfiltration** - All processing on-device
- **User consent required** - Permissions requested explicitly
- **Local-first** - Offline by design

See `.workbuddy/memory/nora-constitution.md` for full details.

## Security

- **No network access** - INTERNET permission explicitly removed
- **User-controlled permissions** - NotificationListener and file access require explicit user grant
- **Local-only storage** - Room database stays on device
- **No telemetry** - Zero analytics or crash reporting

## Contributing

Contributions welcome! Please read our development guidelines:

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Standards

- All phases require Instrument tests passing
- Constitution compliance audit at each Phase Gate
- Minimum 10 Instrument test cases per phase
- Atomic commits per Step

## Roadmap

- [ ] Phase 1-5: Core chat functionality
- [ ] Phase 6: Notification aggregation + AI summarization
- [ ] Phase 7: File context for conversations
- [ ] Phase 8+: Voice input, widget, etc.

## License

MIT License - see [LICENSE](LICENSE) for details.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=HarnessTeam/Nora&type=date&legend=top-left)](https://star-history.com/#HarnessTeam/Nora&type=date&legend=top-left)

---

**Built with care for privacy-conscious users who want AI that respects their data.**
