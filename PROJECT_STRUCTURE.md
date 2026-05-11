# SleepSense — Project Structure (for explaining to professor)

> Two top-level deliverables: an Android app and a backend server. Both live under `v2/`.

---

## 1. Top-level

```
v2/
├── SleepSense/              ← Android app (Kotlin + Jetpack Compose)
└── backend/                 ← Node + Fastify server (TypeScript)
```

**One-liner to say:** *"The project has two parts — an Android app written in Kotlin with Jetpack Compose, and a small Node backend in TypeScript that hosts the AI endpoints."*

---

## 2. Android app — `v2/SleepSense/`

```
SleepSense/
├── app/                     ← The actual Android module
├── gradle/                  ← Gradle wrapper + version catalog
├── build.gradle.kts         ← Root Gradle build script
├── settings.gradle.kts      ← Module declarations
├── gradle.properties        ← Build-time properties (BACKEND_URL, GOOGLE_WEB_CLIENT_ID)
├── local.properties         ← Local SDK path (gitignored)
├── README.md
├── DEVELOPER_GUIDE.md
├── Features.md              ← Full feature reference
├── PRESENTATION_BRIEF.md
├── TODO_DEMO_SPRINT.md
└── PROFESSOR_QA_PREP.md     ← Q&A prep for tomorrow
```

### Inside `app/src/main/java/com/circadianx/sleepsense/`

This is the actual code. We use **clean architecture in three layers**: `data` (where data comes from), `domain` (business rules), and `ui` (what the user sees), plus supporting packages.

```
sleepsense/
├── MainActivity.kt          ← Single-activity entry point; sets up Compose
├── SleepSenseApp.kt         ← Hilt application class
│
├── data/                    ← DATA LAYER — all sources of truth
│   ├── audio/               ← MicrophoneRecorder (mic dBFS capture)
│   ├── auth/                ← GoogleAuthRepository, AuthSessionManager
│   ├── bluetooth/           ← BluetoothManager (ESP32 path, designed for v2)
│   ├── db/                  ← Older DAOs (ApneaEvent, SleepSession)
│   ├── local/
│   │   ├── datastore/       ← UserPreferences (DataStore)
│   │   ├── db/dao/          ← Room DAOs: SleepRecord, Routine, Challenge, etc.
│   │   └── security/        ← PhotoCipher (AES encryption for photos)
│   ├── health/              ← HealthConnectManager (Google Health Connect → steps)
│   ├── model/               ← Data models / entities
│   ├── network/             ← Backend API clients
│   │   ├── AuthApiRepository.kt        → POST /auth/verify
│   │   ├── ChatRepository.kt           → POST /chat
│   │   ├── ReportRepository.kt         → POST /report
│   │   ├── RouteRepository.kt          → POST /routes/suggest
│   │   ├── SpotifyRepository.kt        → /spotify/*
│   │   ├── SyncRepository.kt           → /sync/*
│   │   └── AuthHeaderInterceptor.kt    ← attaches Bearer token to every request
│   ├── preferences/         ← User preference helpers
│   ├── seed/                ← DemoDataSeeder (14 nights of demo data)
│   └── spotify/             ← Spotify token store
│
├── domain/                  ← DOMAIN LAYER — pure business logic
│   ├── model/               ← Domain models (RiskLevel, etc.)
│   ├── repository/          ← Repository interfaces
│   └── usecase/             ← Use cases / business rules
│
├── ui/                      ← UI LAYER — Compose screens
│   ├── components/          ← Reusable widgets: AhiRingCard, StatCard, WeeklyBarChart, etc.
│   ├── screens/             ← One file per screen (15 screens)
│   │   ├── AuthScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── RecordingScreen.kt
│   │   ├── ReportScreen.kt           ← AI Weekly Report (the WOW)
│   │   ├── ChatScreen.kt
│   │   ├── HistoryScreen.kt
│   │   ├── HabitsScreen.kt
│   │   ├── ChallengesScreen.kt
│   │   ├── ProgressPhotosScreen.kt
│   │   ├── StepsScreen.kt
│   │   ├── SocialScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── SpotifyScreen.kt
│   │   └── WalkPlannerScreen.kt
│   └── theme/               ← Color tokens, typography, dark theme
│
├── viewmodel/               ← One ViewModel per screen (MVVM pattern)
│   ├── AuthViewModel.kt
│   ├── DashboardViewModel.kt
│   ├── ReportViewModel.kt
│   ├── HistoryViewModel.kt
│   ├── HabitsViewModel.kt
│   ├── ChallengesViewModel.kt
│   ├── ChatViewModel.kt
│   ├── ProgressPhotosViewModel.kt
│   ├── SettingsViewModel.kt
│   ├── SocialViewModel.kt
│   ├── SpotifyViewModel.kt
│   └── WalkPlannerViewModel.kt
│
├── presentation/            ← Onboarding-specific presentation logic
│
├── navigation/              ← NavGraph.kt — Compose navigation routes
│
├── service/                 ← Android background components
│   ├── SleepTrackingService.kt          ← Foreground service for overnight tracking
│   ├── StepCounterService.kt            ← Foreground service for step sensor
│   ├── AppBlockingAccessibilityService.kt ← Restricts apps after bedtime
│   ├── BlockedAppActivity.kt
│   ├── BootReceiver.kt                  ← Restarts services on device boot
│   └── RoutineReminderReceiver.kt       ← Schedules habit reminder notifications
│
├── worker/                  ← WorkManager background jobs
├── spotify/                 ← Spotify auth flow helpers
├── di/                      ← Hilt dependency-injection modules
└── util/                    ← Utility functions / extensions
```

### Resources (`app/src/main/res/`)

```
res/
├── xml/network_security_config.xml   ← Allows HTTP to local LAN backend
├── values/                           ← Strings, colors, themes
├── drawable/                         ← Vector icons, backgrounds
├── font/                             ← DM Sans, DM Serif Display, JetBrains Mono
└── mipmap-*/                         ← App launcher icons
```

---

## 3. Backend — `v2/backend/`

A small server that hosts AI endpoints (chat, weekly report) and OAuth-protected sync.

```
backend/
├── src/
│   ├── server.ts            ← Fastify entry point, all routes registered here
│   ├── llm.ts               ← OpenAI client + Zod schemas for AI inputs/outputs
│   ├── auth.ts              ← Google OAuth token verification
│   ├── user-store.ts        ← Per-user in-memory store (challenges, habits, Spotify tokens)
│   ├── routes.ts            ← Route-suggest schema
│   ├── spotify.ts           ← Spotify OAuth (PKCE flow)
│   └── server.test.ts       ← Smoke tests
├── package.json             ← Dependencies (fastify, openai, zod, dotenv)
├── tsconfig.json
├── .env                     ← Secrets: OPENAI_API_KEY, OAUTH_AUDIENCES, etc. (gitignored)
└── node_modules/
```

### What each backend route does

| Route | Purpose |
|---|---|
| `GET /health` | Liveness check |
| `POST /auth/verify` | Validates a Google ID token, returns hashed userId |
| `GET / PATCH /users/me` | Profile read/update |
| `POST /chat` | Context-aware AI chat (sleep + habits + steps + goals) |
| `POST /report` | The AI Weekly Report — narrative, patterns, recommendations |
| `POST /insights/daily` | 1–3 daily micro-suggestions |
| `POST /insights/weekly` | Short weekly summary |
| `GET / PUT /sync/challenges` | Sync user challenges across devices |
| `GET / PUT /sync/habits` | Sync user habits across devices |
| `POST /spotify/exchange` | Exchange Spotify auth code for token |
| `GET /spotify/me` | Get Spotify profile (auto-refreshes token) |
| `GET /spotify/open/winddown` | Returns deep link to wind-down playlist |
| `POST /routes/suggest` | Walking-route suggestion via Google Routes API |

---

## 4. How the layers actually talk to each other

```
   USER taps "View AI Report" on Dashboard
                ↓
   ui/screens/DashboardScreen.kt
                ↓ (callback)
   navigation/NavGraph.kt → navigates to Screen.Report
                ↓
   ui/screens/ReportScreen.kt
                ↓ (collectAsStateWithLifecycle)
   viewmodel/ReportViewModel.kt
                ↓ (calls)
   data/network/ReportRepository.kt
                ↓ (HTTP POST /report, with Bearer token via AuthHeaderInterceptor)
   backend/src/server.ts → '/report' handler
                ↓
   backend/src/llm.ts → getOpenAIClient() → OpenAI GPT-4.1-mini
                ↓
   JSON response validated with Zod → cached → returned
                ↓
   Back to ReportRepository → ReportViewModel → ReportScreen renders the cards
```

**One-liner to say:** *"User input flows down through Compose → ViewModel → Repository → backend; data flows back up the same path. Everything in between is reactive — `StateFlow` on the Android side, `Promise` on the backend side."*

---

## 5. Architectural patterns we used

| Pattern | Where | Why |
|---|---|---|
| **MVVM** | `ui/` ↔ `viewmodel/` ↔ `data/` | Standard Android pattern; clean separation of UI state from business logic |
| **Repository pattern** | `data/network/*Repository.kt`, `data/local/db/dao/*Dao.kt` | Single source of truth per data type; lets us swap implementations |
| **Dependency injection (Hilt)** | `di/` | All singletons (`MicrophoneRecorder`, `OkHttpClient`, `AuthSessionManager`) injected, never `new`'d |
| **Reactive streams** | `StateFlow`, `Flow` everywhere | UI auto-updates when data changes; no manual refresh |
| **Foreground services** | `service/` | Android requires foreground service for overnight mic recording |
| **Schema-first API** | `backend/src/llm.ts` (Zod) | LLM responses validated against schema, type-safe end-to-end |
| **Clean architecture (light)** | `data/` ↔ `domain/` ↔ `ui/` | Domain layer holds business rules independent of Android or HTTP |

---

## 6. The 10-second elevator version (memorize this)

> *"The project is split into two parts under the v2 folder. The Android app uses MVVM with Jetpack Compose: each screen has a ViewModel, each ViewModel pulls from a Repository, and the Repository talks either to a Room database locally or to our backend over HTTP. The backend is a small Fastify server in TypeScript that wraps the OpenAI API and adds Google OAuth, response caching, and rate limiting. The data layer is built around 15+ Room entities, all wired through Hilt dependency injection."*

---

## 7. The 30-second walk-through version

> *"At the top level, we have the Android module under `app/`. Inside, the code is organized in four packages: `data` for everything that produces or stores data — that includes Room DAOs, the microphone recorder, the encrypted photo storage, and the network repositories that call our backend. `domain` holds pure business logic — repository interfaces and use cases. `ui` is all Jetpack Compose screens and reusable components. And `viewmodel` connects them via MVVM, with one ViewModel per screen."*
>
> *"On the backend side, `server.ts` is the Fastify entry point and all routes — `/chat`, `/report`, sync endpoints, Spotify OAuth — are registered there. `llm.ts` wraps the OpenAI client with Zod schemas so the AI's JSON output is type-safe. `auth.ts` verifies Google ID tokens. The whole server is under 600 lines of TypeScript."*

---

## 8. Likely follow-up questions about structure

### Q. Why three layers (data/domain/ui)?
*"It's clean architecture, lightly applied. The domain layer holds rules that are independent of Android or HTTP. If we ported to iOS, the domain layer's intent could carry over. In practice for v1 we use it lightly — most logic lives in ViewModels — but the structure is in place."*

### Q. Why one ViewModel per screen?
*"Each screen has its own state and lifecycle. Sharing a ViewModel across screens couples them. The exception is Dashboard ↔ Recording, which deliberately share `DashboardViewModel` so the recording flow can save back to the dashboard's state."*

### Q. Why both `data/db/` and `data/local/db/`?
*"Honest answer: the older `data/db/` package contains legacy DAOs — `ApneaEventDao`, `SleepSessionDao` — from the original ESP32-paired design. The newer `data/local/db/dao/` is where we built the v1 sleep-record DAOs. We'd consolidate them in a refactor sprint."*

### Q. Why so many ViewModels — 12?
*"Each is small — typically 50–100 lines. We chose many small ViewModels over a few large ones to keep state contained. None of them is doing heavy work; they're mostly orchestration between repositories and Compose."*

### Q. What's in `worker/` vs `service/`?
*"`service/` holds long-running Android components — foreground services for tracking, accessibility services for app-blocking, broadcast receivers for boot and reminders. `worker/` holds WorkManager jobs — short, deferrable background tasks. The split follows Android's official guidance."*

### Q. How big is the codebase?
*"Roughly: ~50 Kotlin files in the app, ~7 TypeScript files on the backend, 15+ Room entities, 15 Compose screens. Build configuration is around 80 lines of Kotlin DSL. Total project is on the order of 8–12k lines of code excluding generated files and node_modules."*

---

## 9. Folder names you'll be asked about (rapid fire)

| Folder | What lives there | One-line answer |
|---|---|---|
| `data/audio/` | `MicrophoneRecorder` | "Captures phone mic at 44.1 kHz and emits a flow of dBFS values" |
| `data/auth/` | Google sign-in + session | "Wraps Android's Credential Manager and stores the ID token" |
| `data/bluetooth/` | `BluetoothManager` | "ESP32 path — designed but not wired in v1" |
| `data/local/db/` | Room database | "Local persistence — 15+ entities, single source of truth" |
| `data/local/security/` | `PhotoCipher` | "AES encryption for progress photos at rest" |
| `data/network/` | API clients | "One repository per backend domain — chat, report, sync, Spotify" |
| `data/seed/` | `DemoDataSeeder` | "Generates 14 nights of realistic demo data on first launch" |
| `domain/` | Business rules | "Pure logic, no Android/HTTP dependencies" |
| `ui/components/` | Reusable widgets | "Shared Compose components — score ring, charts, stat cards" |
| `ui/theme/` | Design tokens | "Dark theme — purple accent, three font families" |
| `viewmodel/` | MVVM ViewModels | "One per screen, each holds StateFlow of UI state" |
| `service/` | Background services | "Foreground tracking, accessibility blocking, reminders" |
| `worker/` | WorkManager jobs | "Short deferrable background tasks" |
| `di/` | Hilt modules | "Dependency injection wiring" |
| `navigation/` | NavGraph | "Compose Navigation routes and transitions" |
| `backend/src/server.ts` | Backend entry | "Fastify app with all routes registered" |
| `backend/src/llm.ts` | OpenAI wrapper | "Zod-validated AI input/output schemas" |
| `backend/src/auth.ts` | OAuth | "Verifies Google ID tokens via tokeninfo endpoint" |

---

## 10. If asked to draw the structure on a whiteboard

```
┌─────────────────────────────────────────────────┐
│                  ANDROID APP                    │
│                                                 │
│  ┌─────────────┐                                │
│  │     UI      │  (Jetpack Compose screens)     │
│  └──────┬──────┘                                │
│         │ collectAsState                        │
│  ┌──────▼──────┐                                │
│  │  ViewModel  │  (StateFlow, MVVM)             │
│  └──────┬──────┘                                │
│         │                                       │
│  ┌──────▼──────┐                                │
│  │ Repository  │  ◄── Hilt DI                   │
│  └──┬───────┬──┘                                │
│     │       │                                   │
│  ┌──▼──┐  ┌─▼──────┐                            │
│  │Room │  │ OkHttp │  ── HTTPS ──┐              │
│  │ DB  │  └────────┘             │              │
│  └─────┘                         │              │
└──────────────────────────────────┼──────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────┐
│                BACKEND (Node)                   │
│                                                 │
│  Fastify routes ──► auth.ts (Google OAuth)      │
│         │                                       │
│         ├─► llm.ts ──► OpenAI GPT-4.1-mini      │
│         ├─► user-store.ts (per-user state)      │
│         └─► spotify.ts ──► Spotify Web API      │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

**Final tip:** if the professor asks you to "explain the structure," start with section **6** (the 10-second version), then offer to go deeper. Don't dump the whole tree on them — let them pull on the threads they care about.
