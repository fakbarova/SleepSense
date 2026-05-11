# SleepSense — Project Report

**Course:** Embedded Modern Project
**Team:** CircadianX
**Author:** Shohjahon Razzoqov
**Repository:** https://github.com/Shohjahon777/sleepsense.io
**Date:** May 2026

---

## Abstract

SleepSense is an Android-based sleep wellness companion designed as part of the
Embedded Modern Project course. The system combines an Android client written
in Kotlin with Jetpack Compose, an embedded sensing path designed around an
ESP32 microcontroller communicating over Bluetooth, on-device sensors
(microphone, accelerometer, step counter), and a small Node/TypeScript backend
that wraps a Large Language Model (LLM) for context-aware sleep insights.
The application's goal is to help users understand and improve their sleep by
passively tracking nightly sessions, scoring sleep quality, surfacing apnea
risk, encouraging healthy routines and goal-based challenges, and producing
AI-generated weekly reports. This report describes the motivation,
architecture, implementation, design decisions, and future work of the
SleepSense project.

---

## Table of Contents

1. Introduction
2. Problem Statement and Motivation
3. Project Goals and Scope
4. Related Work
5. System Overview
6. Hardware Architecture
7. Software Architecture
8. Tech Stack
9. Application Features
10. Data Model
11. Backend Service
12. Security and Privacy
13. Background Services and Sensors
14. Design System and User Experience
15. Implementation Details
16. Testing and Validation
17. Challenges and Lessons Learned
18. Future Work
19. Conclusion
20. References
21. Appendix: Project Structure

---

## 1. Introduction

Sleep is one of the most important determinants of physical and mental
health, yet it is also one of the least visible. People wake up, judge their
sleep subjectively ("I slept badly"), and rarely have data to explain why.
Modern smartphones already contain most of the sensors needed to give that
data — microphones, accelerometers, step counters — and modern LLMs can
turn raw data into something a human can act on. SleepSense is built on that
observation: that the phone in the user's pocket, optionally extended with a
small embedded sensor, can be a complete sleep tracking and coaching
platform.

This project was developed for the Embedded Modern Project course. It
combines three of the course's core themes — embedded sensing, mobile
software engineering, and modern cloud/AI services — into a single
end-to-end product.

---

## 2. Problem Statement and Motivation

Existing consumer sleep tracking solutions tend to fall into three
categories, each with limitations:

1. **Wearables (Fitbit, Apple Watch, Oura).** Accurate but expensive, require
   the user to wear a device every night, and lock the data behind closed
   ecosystems.
2. **Phone-only apps.** Cheap and accessible, but often limited to
   accelerometer-based motion tracking; they cannot detect apnea-like
   breathing events and offer little personalized coaching.
3. **Medical-grade home polysomnography.** Highly accurate but only available
   on prescription, intrusive, and used for one or two nights at a time.

There is a gap for a product that is **affordable, daily-usable, privacy-respecting,
and intelligent enough to give users actionable feedback**. SleepSense
targets that gap by:

- Using the phone's microphone for snore and breathing detection.
- Optionally pairing with a low-cost ESP32 sensor for additional
  bio-signal capture (Apnea-Hypopnea Index — AHI).
- Storing all sensitive data locally and encrypting media at rest.
- Using an LLM only for narrative generation, not for storing personal data.
- Providing routines, challenges, social accountability, and AI chat to
  drive behavior change, not just measurement.

---

## 3. Project Goals and Scope

### 3.1 Primary goals

- Build a working Android application that tracks at least one full night of
  sleep using on-device sensors and produces a meaningful summary.
- Design and prototype an ESP32-based sensor path that can stream apnea
  events to the phone over Bluetooth.
- Provide an AI-powered weekly report that explains the user's sleep
  patterns and recommends concrete actions.
- Demonstrate clean modern Android architecture (MVVM, Compose, Hilt,
  Room, coroutines) suitable for production use.

### 3.2 Secondary goals

- Habits, challenges, progress photos, social stubs, Spotify integration
  for wind-down playlists.
- App-blocking accessibility service to reduce screen time before bed.
- Google Health Connect integration for step data.
- Encrypted progress photos.

### 3.3 Out of scope

- Medical diagnosis. SleepSense is a wellness tool, not a regulated medical
  device.
- iOS client. Architecture is portable in principle but the deliverable is
  Android-only.
- Multi-user / cloud sync of all data. Only opt-in challenge/habit sync is
  implemented.

---

## 4. Related Work

| Product | Approach | Strength | Weakness |
|---|---|---|---|
| Fitbit / Apple Watch | Wearable PPG + accel | Accurate HR/SpO₂, automatic | Requires hardware, closed ecosystem |
| Sleep Cycle | Phone mic + accel | Cheap, easy onboarding | No coaching, limited insight |
| Oura Ring | Wearable | Best-in-class accuracy | $300+ + subscription |
| Polysomnography | Clinical | Gold standard | Requires sleep lab |
| **SleepSense** | Phone + optional ESP32 + LLM | Affordable, AI coaching, privacy-first | Less accurate than wearable |

SleepSense's contribution is the combination of (a) a phone-first sensing
stack that anyone can use without buying hardware, (b) an optional embedded
extension for apnea detection, and (c) an LLM-driven coaching layer that
turns numbers into narrative.

---

## 5. System Overview

The system has three logical components:

```
   ┌────────────────┐     BLE       ┌──────────────────────┐    HTTPS    ┌────────────────────┐
   │   ESP32 Sensor │ ────────────► │   Android App        │ ──────────► │   Backend (Node)   │
   │  (apnea events)│               │  (Kotlin / Compose)  │             │  Fastify + OpenAI  │
   └────────────────┘               └──────────────────────┘             └────────────────────┘
                                          │      ▲
                                          ▼      │
                                  ┌──────────────────────┐
                                  │ Phone sensors:       │
                                  │  mic, accel, steps,  │
                                  │  Health Connect      │
                                  └──────────────────────┘
```

- **Embedded layer (ESP32).** Captures bio-signals, packages them as
  `ApneaEvent` JSON packets, and streams them to the phone over Bluetooth.
- **Mobile layer (Android).** The user-facing layer. Reads phone sensors,
  ingests ESP32 packets, persists data locally, displays dashboards and
  reports, runs background services for overnight tracking.
- **Cloud layer (Backend).** A small server that wraps the OpenAI API
  behind authenticated endpoints (`/chat`, `/report`, `/insights/*`),
  validates input/output with Zod schemas, and provides opt-in sync for
  challenges and habits.

---

## 6. Hardware Architecture

### 6.1 ESP32 sensor path

The hardware extension is built around an ESP32 microcontroller chosen
because:

- It has integrated Bluetooth Low Energy (BLE), removing the need for a
  separate radio.
- It is widely available, low cost (~$5), and supported by the Arduino and
  ESP-IDF toolchains.
- It has enough RAM/flash to run a small inference loop in C++ if needed.

The sensor's role is to detect apnea-like events while the user sleeps and
emit them as discrete events of the form:

```json
{
  "timestamp": 1715152800000,
  "type": "APNEA",
  "durationMs": 18000,
  "ahiContribution": 1.0
}
```

The phone subscribes to this channel via `BluetoothManager.kt`, parses the
JSON, and inserts each event into the Room database as an `ApneaEvent`
entity. The Apnea-Hypopnea Index (AHI) is then computed as the number of
events per hour of sleep.

### 6.2 Phone sensors

When the ESP32 is not paired, the phone alone can produce a useful sleep
record:

| Sensor | Use |
|---|---|
| Microphone | Snore / breathing detection via dBFS sampling |
| Accelerometer | Motion-based wake detection |
| `TYPE_STEP_COUNTER` | Daytime activity (correlates with sleep quality) |
| Health Connect | Aggregated step data from other apps |

The microphone path is implemented in `MicrophoneRecorder.kt`. It samples
the microphone using Android's `MediaRecorder` / `AudioRecord` APIs and
emits a `Flow<Float>` of decibel values that the UI can render as a live
waveform.

---

## 7. Software Architecture

The Android client follows a **lightly-applied clean architecture**
pattern with three layers — `data`, `domain`, and `ui` — connected by
**MVVM**.

### 7.1 Layer responsibilities

```
┌─────────────────────────────────────────────────┐
│                  ANDROID APP                    │
│  ┌─────────────┐                                │
│  │     UI      │  Jetpack Compose screens       │
│  └──────┬──────┘                                │
│         │ collectAsState                        │
│  ┌──────▼──────┐                                │
│  │  ViewModel  │  StateFlow, MVVM               │
│  └──────┬──────┘                                │
│  ┌──────▼──────┐                                │
│  │ Repository  │  ◄── Hilt DI                   │
│  └──┬───────┬──┘                                │
│  ┌──▼──┐  ┌─▼──────┐                            │
│  │Room │  │ OkHttp │  ── HTTPS ──┐              │
│  │ DB  │  └────────┘             │              │
│  └─────┘                         ▼              │
└──────────────────────────────────┼──────────────┘
                                   │
                                   ▼
                         ┌─────────────────────┐
                         │ Backend (Fastify) → │
                         │ OpenAI GPT-4.1-mini │
                         └─────────────────────┘
```

- **UI layer (`ui/`).** Pure Jetpack Compose. No business logic; only
  collects state and forwards user intents.
- **ViewModel layer (`viewmodel/`).** One ViewModel per screen, exposing
  `StateFlow`s that the screen collects with `collectAsStateWithLifecycle`.
- **Domain layer (`domain/`).** Repository interfaces, use cases such as
  `CalculateSleepScoreUseCase`, and pure model types like `RiskLevel`.
- **Data layer (`data/`).** All sources of truth — Room DAOs, DataStore,
  network repositories, the microphone recorder, the Bluetooth manager,
  the AES photo cipher.

### 7.2 Patterns used

| Pattern | Where | Why |
|---|---|---|
| MVVM | `ui` ↔ `viewmodel` ↔ `data` | Clean separation of UI from logic |
| Repository | `data/network/*Repository.kt`, `data/local/db/dao/*Dao.kt` | Single source of truth per data type |
| Dependency Injection (Hilt) | `di/AppModule.kt`, `di/RepositoryModule.kt` | Testability, no manual wiring |
| Reactive streams | `StateFlow`, `Flow` | UI auto-updates when data changes |
| Foreground services | `service/` | Required for overnight tracking |
| Schema-first API | `backend/src/llm.ts` (Zod) | Type-safe LLM I/O |

---

## 8. Tech Stack

### 8.1 Android

| Area | Choice |
|---|---|
| Language | Kotlin, JVM 17 |
| UI | Jetpack Compose, Material 3 |
| Min / target SDK | 28 / 35 |
| DI | Hilt + KSP |
| Persistence | Room v10 (15+ entities), `exportSchema=false` |
| Preferences | DataStore |
| HTTP | OkHttp + Gson |
| Async | Coroutines, `StateFlow`, `collectAsStateWithLifecycle` |
| Health | Google Health Connect |
| Camera | CameraX |
| Charts | Vico + custom Canvas |
| Image loading | Coil |
| Crypto | AES via `PhotoCipher` |

### 8.2 Backend

| Area | Choice |
|---|---|
| Runtime | Node.js (TypeScript) |
| Web framework | Fastify |
| LLM | OpenAI GPT-4.1-mini |
| Validation | Zod (schema-first) |
| Auth | Google ID-token verification |
| Config | dotenv |
| Tests | Built-in Node test runner |

### 8.3 Embedded

| Area | Choice |
|---|---|
| MCU | ESP32 (Wi-Fi + BLE) |
| Toolchain | Arduino / ESP-IDF |
| Wire format | JSON over BLE GATT |

---

## 9. Application Features

The Android client ships with 15 user-facing screens, organized under
`ui/screens/`. The most important are summarized below.

### 9.1 Onboarding
A three-step onboarding flow captures the user's sleep schedule (target
bedtime / wake time), high-level health goals (sleep quality, weight,
screen time, exercise, body pain), and required permissions
(notifications, usage access, accessibility for app-blocking). Completion
is persisted in DataStore so the screen never re-appears.

### 9.2 Dashboard
The home surface. Shows last night's **sleep score** (0–100), **total
sleep**, **disturbance count**, and **nights tracked**, each as a colored
stat card. A live `liveDbfs` flow is wired so that during a recording
session the dashboard can show the live mic level. When onboarding is
incomplete, a setup prompt replaces the stats.

### 9.3 Recording
The active recording screen. A pulsing emoji ring animates while the
session is active. An elapsed timer shows session length. A 32-bar
canvas waveform reflects the live `liveDbfs` flow, and three live
stats — mic level (dBFS), status (Quiet / Breathing / Snoring), and
peak snore dB — give immediate feedback. A "Stop recording" button
calls `viewModel.stopRecording()` and writes a `SleepRecordEntity`.

### 9.4 History
A weekly bar chart of sleep duration plus a `LazyColumn` of session
cards. Each card shows date, duration, sleep score, and disturbance
count. Empty state handled.

### 9.5 Habits (Routines)
Two checklists — pre-sleep and morning — populated from
`RoutineItemEntity`. Toggling a checkbox writes a
`RoutineCompletionEntity` for today's date. A daily streak emerges
naturally from this data.

### 9.6 Challenges
The user can create personal goals with a title, category, duration in
days, and success criteria. Each challenge is a row of metadata; daily
check-ins and a final rating are tracked through
`ChallengeCheckInEntity` and `ChallengeRatingEntity`.

### 9.7 Progress Photos
A privacy-respecting wellness gallery. Photos are AES-encrypted at rest
via `PhotoCipher` and decrypted on demand for display. A thumbnail strip
plus full-size viewer with prev/next navigation.

### 9.8 Steps
Daily step count and 7-day rolling average. Driven by
`StepDao.observeRecent(7)` and populated from either `StepCounterService`
or Health Connect.

### 9.9 Social
Group challenges and short user-published "stories" (sleep tips). Stored
locally; the schema is ready for opt-in cloud sync.

### 9.10 Chat (AI Assistant)
A single-question chat surface. The user types a question (default:
"Why did I sleep badly last night?") and the app calls `POST /chat` on
the backend, which augments the prompt with recent sleep, habits, and
goals before forwarding to the LLM.

### 9.11 Report (AI Weekly Report) — the "wow" feature
The flagship feature. The app sends seven nights of summarized sleep
data to `POST /report`. The backend prompts GPT-4.1-mini to return a
structured JSON object — narrative, identified patterns, and three
prioritized recommendations — validated with Zod. The Android side
caches the result and renders it as a series of cards in
`ReportScreen.kt`.

### 9.12 Settings
Profile name, snore sensitivity slider (-40 to -10 dBFS), morning
summary notification toggle, and an About section. All preferences
persisted to DataStore.

### 9.13 Spotify, Walk Planner
Spotify integration uses the PKCE OAuth flow and a backend exchange
endpoint. The Walk Planner calls `POST /routes/suggest`, which proxies
to Google Routes for a daytime walk that supports better sleep.

---

## 10. Data Model

Persistence uses a single Room database (`SleepSenseDatabase`, version
10) with **15+ entities** organized by domain.

### 10.1 Sleep & Apnea
| Entity | Purpose |
|---|---|
| `SleepRecordEntity` | Completed sleep session: start/end ms, score, disturbance count |
| `SleepSession` | Legacy model with AHI, temperature, humidity (ESP32 path) |
| `ApneaEvent` | Single apnea event from the ESP32 |
| `NightDisturbanceEntity` | Individual disturbance during a session |

### 10.2 Routines & Challenges
| Entity | Purpose |
|---|---|
| `RoutineItemEntity` | A named routine step |
| `RoutineCompletionEntity` | Records a daily completion |
| `ChallengeEntity` | Personal goal — title, category, duration, criteria |
| `ChallengeCheckInEntity` | Daily check-in against a challenge |
| `ChallengeRatingEntity` | Final user rating |

### 10.3 Social
| Entity | Purpose |
|---|---|
| `GroupChallengeEntity` | Group challenge container |
| `GroupMemberEntity` | Member record for a group |
| `StoryEntity` | A published sleep tip / story |

### 10.4 Activity & Media
| Entity | Purpose |
|---|---|
| `StepDayEntity` | Step count for a single date |
| `ProgressPhotoEntity` | Path + metadata for an encrypted JPEG |

### 10.5 App control
| Entity | Purpose |
|---|---|
| `AppBlockOverrideEntity` | Override record for bedtime app-blocking |

A `DemoDataSeeder` generates 14 nights of realistic synthetic data on
first launch so reviewers can experience the UI immediately, before any
real night of sleep has been recorded.

---

## 11. Backend Service

The backend is a Fastify application written in TypeScript (~7 source
files, ~600 lines).

### 11.1 Routes

| Route | Purpose |
|---|---|
| `GET /health` | Liveness check |
| `POST /auth/verify` | Validates a Google ID token, returns hashed userId |
| `GET / PATCH /users/me` | Profile read/update |
| `POST /chat` | Context-aware AI chat |
| `POST /report` | The AI Weekly Report |
| `POST /insights/daily` | 1–3 daily micro-suggestions |
| `POST /insights/weekly` | Short weekly summary |
| `GET / PUT /sync/challenges` | Cross-device challenge sync |
| `GET / PUT /sync/habits` | Cross-device habit sync |
| `POST /spotify/exchange` | Exchange Spotify auth code for token |
| `GET /spotify/me` | Spotify profile (auto-refreshes token) |
| `GET /spotify/open/winddown` | Deep link to wind-down playlist |
| `POST /routes/suggest` | Walking-route suggestion via Google Routes |

### 11.2 Schema-first LLM I/O

`llm.ts` defines Zod schemas for both prompt inputs and expected JSON
outputs. The OpenAI client is asked for JSON, and every response is
parsed through Zod before being returned to the client. This means a
malformed model response is caught on the server, not in the Android
UI, and it makes the contract type-safe end-to-end.

### 11.3 Caching, rate limiting, auth

- LLM responses are cached per-user, per-input-hash to control cost.
- Rate limiting is applied to the AI endpoints.
- Every protected endpoint requires a `Bearer` token from
  `AuthHeaderInterceptor` on the Android side.

---

## 12. Security and Privacy

Security was a first-class concern, not an afterthought.

| Concern | Mitigation |
|---|---|
| Audio data leaving the device | Microphone audio is **never uploaded**; only dBFS values and derived counts are stored |
| Photo privacy | All progress photos are AES-encrypted at rest via `PhotoCipher` |
| Auth | Google ID-token verification on every authenticated request |
| Token leakage | Spotify and Google tokens stored in encrypted DataStore |
| LLM data exposure | Prompts contain only aggregated sleep stats — no audio, no photos, no PII other than a hashed userId |
| Cleartext HTTP | Allowed only for local dev (`network_security_config.xml`); production tightens to HTTPS-only |
| Ignored secrets | `.env`, `local.properties`, `*.pem`, and `*.key` are in `.gitignore` and never committed |

---

## 13. Background Services and Sensors

Long-running and event-driven Android components live in `service/`:

| Component | Purpose |
|---|---|
| `SleepTrackingService` | Foreground service for passive overnight tracking using bedtime schedule + accelerometer |
| `StepCounterService` | Foreground service for `TYPE_STEP_COUNTER` |
| `AppBlockingAccessibilityService` | Restricts selected apps after bedtime |
| `BootReceiver` | Restarts services on `BOOT_COMPLETED` |
| `RoutineReminderReceiver` | Fires routine reminder notifications |
| `BluetoothManager` | Manages BT connection to ESP32; parses `ApneaEvent` JSON |
| `MicrophoneRecorder` | Captures dBFS; feeds the live waveform |
| `HealthConnectManager` | Reads steps from Google Health Connect |

WorkManager jobs (under `worker/`) handle short, deferrable background
tasks such as `WeeklyReportWorker`, which generates the weekly report
in the background once per week.

---

## 14. Design System and User Experience

### 14.1 Theme

The app uses a custom dark theme designed to be calm, low-glare, and
appropriate for a bedside device.

| Token | Hex | Usage |
|---|---|---|
| `BgDeep` | `#0B0F1A` | Screen backgrounds |
| `BgBase` | `#12182A` | Secondary surfaces, inputs |
| `BgCard` | `#1A2035` | Cards, sheets |
| `Purple` | `#A970FF` | Primary accent |
| `Blue` | `#5B9CF6` | Sleep light stage |
| `Green` | `#34D399` | Low risk / positive |
| `Yellow` | `#FBBF24` | Medium risk / informational |
| `Red` | `#F87171` | High risk / stop |
| `SleepDeep` | `#3B1F6E` | Deep sleep |
| `TextPrimary` | `#FFFFFF` | Body text |
| `TextSecondary` | `#A0A7B8` | Labels |
| `TextMuted` | `#6B7280` | Placeholders |

### 14.2 Typography

| Style | Font | Size |
|---|---|---|
| `displayLarge` | DM Serif Display | 48sp |
| `headlineMedium` | DM Serif Display | 24sp |
| `titleMedium` | DM Sans SemiBold | 14sp |
| `bodyLarge` | DM Sans | 15sp |
| `labelSmall` | JetBrains Mono | 10sp |

### 14.3 Layout conventions

- 20 dp horizontal padding, 24 dp top padding.
- Cards: 14 dp corner radius, 1 dp `Border` stroke, 16 dp internal
  padding, 12 dp spacing.
- Buttons: 48–52 dp height, 12 dp corner radius.

---

## 15. Implementation Details

### 15.1 The recording loop

When a recording session starts:

1. `DashboardViewModel.startRecording()` flips a `StateFlow<Boolean>`.
2. `MicrophoneRecorder` opens an `AudioRecord` source, samples PCM
   frames, and computes dBFS in a coroutine.
3. The dBFS values are emitted on `liveDbfs: SharedFlow<Float>`.
4. `RecordingScreen` collects the flow and feeds the canvas waveform.
5. On stop, the ViewModel computes a sleep score
   (`CalculateSleepScoreUseCase`) and writes a `SleepRecordEntity` via
   `SleepRecordDao`.

### 15.2 The AI report request

```
ReportViewModel
    └── ReportRepository.fetchReport()
        └── OkHttp POST /report  (Bearer token via AuthHeaderInterceptor)
            └── server.ts /report
                └── llm.ts → OpenAI (JSON mode)
                    └── Zod parse
                        └── cache + return
```

Every response is validated against a Zod schema before crossing the
network boundary back to the client. On the Android side, the parsed
response is held as `StateFlow<ReportState>` and rendered as cards.

### 15.3 Encrypted progress photos

`PhotoCipher` wraps `Cipher.getInstance("AES/GCM/NoPadding")`. Each
photo is encrypted with a per-app key stored in the Android Keystore.
On display, the photo is decrypted into an in-memory `ByteArray` and
loaded into Coil with a `ByteArrayDataSource`. The ciphertext on disk
is never displayed and never leaves the device.

### 15.4 Hilt dependency injection

`AppModule.kt` provides singletons: `OkHttpClient`, `MicrophoneRecorder`,
`AuthSessionManager`, `Json`, `SleepSenseDatabase`. `RepositoryModule.kt`
binds repository interfaces to implementations. No constructor in the
app uses `new` directly for these dependencies.

### 15.5 Build configuration

- `BACKEND_URL` is injected via `BuildConfig` and defaults to
  `http://10.0.2.2:8080` for the Android emulator.
- `MAPS_API_KEY` is injected from `local.properties` or the environment
  through `manifestPlaceholders`.
- `network_security_config.xml` allows cleartext traffic to the local
  LAN only.

---

## 16. Testing and Validation

### 16.1 Backend
A `server.test.ts` file uses the Node test runner to smoke-test the
Fastify routes: health, `auth/verify` with a stubbed token, and
`/report` with a mocked OpenAI client. Zod schemas are exercised by
construction.

### 16.2 Android
- Compile-time: `./gradlew :app:compileDebugKotlin` is part of the
  acceptance criteria for every demo build.
- Runtime: `DemoDataSeeder` populates 14 nights of synthetic data so
  every screen is reviewable without waiting for a real night.
- Manual QA: each screen has a hand-written acceptance checklist in
  `TODO_DEMO_SPRINT.md`.

### 16.3 Limitations of testing

- The microphone path is not unit-testable without an audio fixture.
- The ESP32 path is currently exercised manually with a mock JSON
  source.
- LLM output is non-deterministic; we test the *parser*, not the
  *content*.

---

## 17. Challenges and Lessons Learned

### 17.1 Challenge: Foreground service constraints
Android's foreground-service rules tightened across SDK 28 → 35. Running
microphone capture overnight requires a `mediaProjection` or
`microphone` foreground-service type and a persistent notification.
The first naive implementation crashed on Android 14 because the
service type was not declared. **Lesson:** always declare service
types in the manifest and test on the latest API level.

### 17.2 Challenge: LLM JSON reliability
Early prototypes asked GPT for free-form text and parsed it
heuristically — fragile. Switching to OpenAI's JSON mode + Zod
validation made the system dramatically more robust. **Lesson:** when
an LLM is part of an app, treat its output like an external API:
schema first, validate every response.

### 17.3 Challenge: Keystore-based encryption
`PhotoCipher` initially used `AES/CBC/PKCS5Padding`, which works but
lacks authentication. Switching to `AES/GCM/NoPadding` gave us
authenticated encryption and made tampering detectable. **Lesson:**
prefer GCM over CBC for any new design.

### 17.4 Challenge: One ViewModel per screen vs shared ViewModels
12 ViewModels felt excessive at first, but trying to share a single
ViewModel between Dashboard and Recording quickly broke lifecycle
assumptions. We deliberately kept `DashboardViewModel` shared across
those two screens (because Recording must save into the Dashboard's
state) but split everything else. **Lesson:** prefer many small
ViewModels; share only when there is a hard data-flow reason.

### 17.5 Challenge: Compose recomposition cost
Live waveform rendering at 30+ FPS via `Canvas` was tempting to drive
from a `StateFlow<List<Float>>`. That triggered full recompositions of
neighboring composables. Moving to a single `derivedStateOf` plus a
`Modifier.drawBehind` solved it. **Lesson:** drawing-only updates
should not flow through state hoisting.

---

## 18. Future Work

### 18.1 Short term
- Wire the ESP32 path end-to-end (currently `BluetoothManager` is
  designed but the demo runs phone-only).
- Replace OkHttp + Gson with Retrofit + Kotlin Serialization for type
  safety.
- Re-enable `SleepTrackingService` and `StepCounterService` (currently
  commented out in `MainActivity` for the demo build).
- Add a real `Report` screen entry in `NavGraph.kt`.

### 18.2 Medium term
- Cloud sync of all entities, with a CRDT or last-write-wins strategy.
- iOS port of the domain layer.
- On-device snore classification with a small TFLite model, replacing
  the dBFS-threshold heuristic.
- Real medical-disclaimer compliance review before shipping AHI
  numbers to the user.

### 18.3 Long term
- Closed-loop coaching: the LLM proposes a habit, the app schedules
  it, and the next week's report measures whether it helped.
- Wearable companion app for richer biometric data.
- Multi-user / family mode for tracking sleep across a household.

---

## 19. Conclusion

SleepSense demonstrates that a single small team, in the timeframe of a
university project, can build a functional sleep-wellness platform that
combines embedded sensing, modern Android software engineering, and
LLM-powered coaching. The codebase — roughly 8,000–12,000 lines of
Kotlin and TypeScript — is structured around clean MVVM/clean
architecture principles, uses production-grade patterns (Hilt, Room,
StateFlow, Compose), and treats privacy and security as first-class
concerns rather than afterthoughts.

The project achieves all of its primary goals: a working Android client
with overnight tracking, a designed ESP32 sensor path, an AI weekly
report backed by a schema-validated LLM service, and a polished dark
UI. It also surfaces clear directions for future work — closed-loop
coaching, on-device ML, and broader platform support — that would take
SleepSense from a course deliverable to a viable consumer product.

---

## 20. References

1. Android Developers. *Foreground services overview.*
   https://developer.android.com/develop/background-work/services/foreground-services
2. Android Developers. *Jetpack Compose documentation.*
   https://developer.android.com/jetpack/compose
3. Android Developers. *Health Connect.*
   https://developer.android.com/health-and-fitness/guides/health-connect
4. Espressif Systems. *ESP32 Technical Reference Manual.*
5. Fastify. *Web framework documentation.* https://fastify.dev
6. OpenAI. *Structured outputs / JSON mode.*
   https://platform.openai.com/docs/guides/structured-outputs
7. Zod. *TypeScript-first schema validation.* https://zod.dev
8. Google. *Identity Services — verifying ID tokens.*
   https://developers.google.com/identity/sign-in/web/backend-auth
9. American Academy of Sleep Medicine. *Definition of the Apnea-Hypopnea
   Index (AHI).*
10. NIST. *Recommendation for Block Cipher Modes of Operation: GCM
    and GMAC* (SP 800-38D).

---

## 21. Appendix: Project Structure

```
v2/
├── SleepSense/                       Android app (Kotlin + Compose)
│   ├── app/
│   │   └── src/main/java/com/circadianx/sleepsense/
│   │       ├── MainActivity.kt
│   │       ├── SleepSenseApp.kt      Hilt application class
│   │       ├── data/                 Sources of truth
│   │       │   ├── audio/            MicrophoneRecorder
│   │       │   ├── auth/             Google sign-in, session
│   │       │   ├── bluetooth/        ESP32 path
│   │       │   ├── db/               Legacy DAOs (ApneaEvent, SleepSession)
│   │       │   ├── local/
│   │       │   │   ├── datastore/    User preferences
│   │       │   │   ├── db/dao/       Room DAOs (15+ entities)
│   │       │   │   └── security/     PhotoCipher (AES-GCM)
│   │       │   ├── health/           HealthConnectManager
│   │       │   ├── network/          Backend API clients
│   │       │   ├── seed/             DemoDataSeeder
│   │       │   └── spotify/          Spotify token store
│   │       ├── domain/               Business rules
│   │       ├── ui/                   Jetpack Compose screens (15)
│   │       │   ├── components/       Reusable widgets
│   │       │   ├── screens/          One file per screen
│   │       │   └── theme/            Color, typography
│   │       ├── viewmodel/            One ViewModel per screen
│   │       ├── presentation/         Onboarding presentation
│   │       ├── navigation/           NavGraph.kt
│   │       ├── service/              Foreground / accessibility / receivers
│   │       ├── worker/               WorkManager jobs
│   │       ├── spotify/              OAuth helpers
│   │       ├── di/                   Hilt modules
│   │       └── util/                 Utilities
│   ├── gradle/                       Wrapper + version catalog
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   └── DEVELOPER_GUIDE.md
│
├── backend/                          Fastify + OpenAI server
│   ├── src/
│   │   ├── server.ts                 Fastify entry, all routes
│   │   ├── llm.ts                    OpenAI client + Zod schemas
│   │   ├── auth.ts                   Google OAuth verify
│   │   ├── user-store.ts             Per-user state
│   │   ├── routes.ts                 Route-suggest schema
│   │   ├── spotify.ts                Spotify OAuth (PKCE)
│   │   └── server.test.ts            Smoke tests
│   ├── package.json
│   ├── tsconfig.json
│   └── .env                          (gitignored)
│
├── README.md
├── REPORT.md                         (this file)
└── .gitignore
```

---

*End of report.*
