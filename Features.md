# SleepSense — Feature Reference

## Overview

SleepSense is an Android sleep-wellness companion app built by CircadianX as part of an embedded IoT course project. It pairs with an ESP32-based hardware sensor over Bluetooth to passively detect sleep apnea events (AHI), while also using the phone's microphone for snore detection, the accelerometer for motion tracking, and Google Health Connect for step data. The app combines sleep analytics, personal routines, goal challenges, social accountability, progress photos, and an AI chat assistant into one dark-themed experience designed around a healthy sleep routine.

---

## Tech Stack

- **Language:** Kotlin (JVM 17)
- **UI:** Jetpack Compose + Material 3 (min SDK 28, target SDK 35)
- **Dependency Injection:** Hilt (KSP)
- **Persistence:** Room (v10 schema, KSP, 15+ entities)
- **Preferences:** DataStore (encrypted)
- **Background tasks:** WorkManager + Foreground Services
- **Health data:** Google Health Connect API (steps)
- **Camera:** CameraX (progress photo capture)
- **Image loading:** Coil
- **Charts:** Vico (bar charts)
- **Serialization:** Gson
- **Security:** AES encryption for progress photos (`PhotoCipher`)
- **Connectivity:** Android Bluetooth (ESP32 ApneaEvent ingest)
- **Audio:** Android `MediaRecorder` / mic dBFS capture (`MicrophoneRecorder`)
- **Coroutines:** `kotlinx.coroutines` + `StateFlow` + `collectAsStateWithLifecycle`

---

## Screens & Features

### Onboarding (3 steps)

First-run setup that auto-navigates to Dashboard once complete. `OnboardingScreen.kt`

- **Step 1 — Sleep schedule:** User picks target bedtime and wake time via `TimePickerDialog`. Times are stored via `OnboardingViewModel` → DataStore and used by `SleepTrackingService` for passive detection.
- **Step 2 — Goals:** Multi-select checklist of health goals (improve sleep quality, lose weight, decrease screen time, build exercise habit, reduce body pain). Stored to DataStore.
- **Step 3 — Permissions:** Cards for Notifications (runtime permission, Android 13+), Usage Access (system settings), and Accessibility (system settings for app-blocking feature). Best-effort; app works without them.
- Progress dots and a single CTA button. Navigates to Dashboard on completion. Completion state persisted in DataStore — does not re-show on re-launch.

---

### Dashboard

Home screen showing last night at a glance. `DashboardScreen.kt` + `DashboardViewModel.kt`

- **Sleep score** stat card (0–100, purple accent)
- **Total sleep** stat card (formatted as `Xh Ym`, blue accent)
- **Disturbances** stat card (green/red accent, deltaPositiveIsBad)
- **Nights tracked** stat card (yellow accent)
- Setup prompt shown when onboarding schedule is not yet set.
- "View morning summary" button (navigates to morning routine — currently a no-op placeholder).
- Reads from `SleepRecordEntity` via `DashboardViewModel` → `HistoryDao`.
- `liveDbfs` state flow ready for `MicrophoneRecorder` integration (TODO wired comment in ViewModel).

---

### History

Chronological sleep log with chart. `HistoryScreen.kt` + `HistoryViewModel.kt`

- **Weekly bar chart** — Vico `ColumnChart` of sleep duration per day for the last 7 days. `WeeklyBarChart.kt` component.
- **Session list** — `LazyColumn` of `SleepRecordListItem` cards. Each shows date, duration, sleep score (`/100`), and disturbance count.
- Empty state when no sessions exist.
- Data source: `HistoryDao.observeRecentRecords()` → `SleepRecordEntity`.

---

### Routines (Habits)

Daily checklist for pre-sleep and morning habits. `HabitsScreen.kt` + `HabitsViewModel.kt`

- Two sections: **Pre-sleep** and **Morning**.
- Each routine item rendered as a tappable `RoutineRow` with `Checkbox` and label.
- Toggling marks a `RoutineCompletionEntity` for today's date.
- Data: `RoutineItemEntity` (static items) + `RoutineCompletionEntity` (daily completions) via `RoutineDao`.

---

### Challenges

Personal goal commitment with duration and success criteria. `ChallengesScreen.kt` + `ChallengesViewModel.kt`

- List of active `ChallengeEntity` cards showing title, category, duration in days, and success criteria text.
- "Create a challenge" button triggers `CreateChallengeSheet` modal with fields: title, category (sleep / exercise / etc.), duration in days, success criteria.
- Empty state when no challenges exist.
- Data: `ChallengeDao` → `ChallengeEntity`, `ChallengeCheckInEntity`, `ChallengeRatingEntity`.

---

### Progress Photos

Encrypted wellness progress gallery. `ProgressPhotosScreen.kt` + `ProgressPhotosViewModel.kt`

- **Viewer:** Displays current photo full-size (320dp height) with Prev / Next navigation.
- **Thumbnail strip:** `LazyRow` of decrypted JPEG thumbnails.
- Photos are stored AES-encrypted via `PhotoCipher`; decrypted on-the-fly for display.
- Photo counter label ("Photo X/Y").
- Empty state with instructions to take a photo inside a challenge.
- Data: `ProgressPhotoDao` → `ProgressPhotoEntity`.

---

### Steps

Daily activity tracking. `StepsScreen.kt` + `StepsViewModel.kt`

- **Today's step count** displayed as a large serif number.
- **7-day rolling average** shown as secondary text.
- Data comes from `StepDao.observeRecent(7)` → `StepDayEntity`.
- Background data populated by `StepCounterService` (foreground service, currently commented out) and/or Health Connect.

---

### Social

Group challenges and sleep stories. `SocialScreen.kt` + `SocialViewModel.kt`

- **Groups section:** Inline form to create a group challenge (name field + "Create group" button). Existing groups listed as `GroupRow` cards with a "Local" tag.
- **Stories section:** Inline form to publish a story (title + multi-line body). Existing stories listed as `StoryCard` cards with title and body text.
- Data: `GroupChallengeEntity`, `GroupMemberEntity`, `StoryEntity` via `SocialDao`.

---

### Chat (AI Assistant)

AI-powered Q&A about your sleep. `ChatScreen.kt`

- Single `OutlinedTextField` for the user's question, pre-filled with "Why did I sleep badly last night?"
- "Ask" button — wired UI but backend network call is a Phase 3 TODO.
- Placeholder note: "Wiring to backend is ready; the Android network call is the next step."

---

### Recording

Real-time sleep recording session UI. `RecordingScreen.kt`

- Full-screen dark UI entered via navigation.
- **Pulsing ring** animation (scale 0.94–1.06, 1200ms) around the sleep emoji.
- **Elapsed timer** formatted as `MM:SS` or `H:MM:SS`.
- **Live waveform** — 32-bar `Canvas` amplitude chart driven by `liveDbfs` flow from `DashboardViewModel`.
- **Live stats row:** Mic level (dBFS), Status label (Quiet / Breathing / Snoring), Snore dB.
- **Stop recording** button (red) — calls `viewModel.stopRecording()` then pops back.

---

### Settings

User preferences. `SettingsScreen.kt` + `SettingsViewModel.kt`

- **Profile section:** Name input field, persisted to DataStore.
- **Microphone section:** "Snore sensitivity" `Slider` (-40 to -10 dBFS), value shown as `JetBrainsMono` label. Help text explains direction.
- **Notifications section:** "Morning summary" `Switch` toggle, persisted to DataStore.
- **About section:** App version (SleepSense v1.0), team (CircadianX), recording method (Phone microphone).

---

### Report (unrouted)

PDF sleep report export. `ReportScreen.kt`

- Exists in the codebase but is not registered in the current navigation graph.
- Intended as a shareable summary; PDF export logic present.

---

## Data Layer

### Sleep & Apnea
| Entity | Purpose |
|---|---|
| `SleepRecordEntity` | Completed sleep session (start/end ms, score, disturbances) |
| `SleepSession` | Legacy model with AHI, temperature, humidity |
| `ApneaEvent` | Single apnea event from ESP32 via Bluetooth |
| `NightDisturbanceEntity` | Individual disturbance event during a session |

### Routines
| Entity | Purpose |
|---|---|
| `RoutineItemEntity` | A named routine step (pre-sleep or morning) |
| `RoutineCompletionEntity` | Records a completion for a specific day |

### Challenges & Goals
| Entity | Purpose |
|---|---|
| `ChallengeEntity` | A personal goal with title, category, duration, criteria |
| `ChallengeCheckInEntity` | Daily check-in against a challenge |
| `ChallengeRatingEntity` | User rating of a completed challenge |

### Social
| Entity | Purpose |
|---|---|
| `GroupChallengeEntity` | A named group challenge |
| `GroupMemberEntity` | Member record for a group |
| `StoryEntity` | A published sleep tip story |

### Activity
| Entity | Purpose |
|---|---|
| `StepDayEntity` | Step count for a single date |

### Media
| Entity | Purpose |
|---|---|
| `ProgressPhotoEntity` | Path + metadata for an encrypted JPEG |

### App Control
| Entity | Purpose |
|---|---|
| `AppBlockOverrideEntity` | Override record for bedtime app-blocking |

---

## Background Services & Hardware

| Component | Purpose |
|---|---|
| `SleepTrackingService` | Foreground service — passive sleep/wake detection using bedtime schedule + motion sensors (Phase 1, currently commented out in MainActivity) |
| `StepCounterService` | Foreground service — step counting via `TYPE_STEP_COUNTER` sensor (Phase 1, commented out) |
| `AppBlockingAccessibilityService` | Accessibility service — restrict selected apps after bedtime |
| `BootReceiver` | `BOOT_COMPLETED` broadcast receiver — re-starts services after device reboot |
| `RoutineReminderReceiver` | Alarm-based broadcast — fires routine reminder notifications at scheduled times |
| `BluetoothManager` | Manages BT connection to ESP32; parses `ApneaEvent` JSON packets |
| `MicrophoneRecorder` | Captures mic dBFS; feeds `liveDbfs` flow for waveform / snore detection |
| `HealthConnectManager` | Reads step data from Google Health Connect |

---

## Design System

### Color Tokens
| Token | Hex | Usage |
|---|---|---|
| `BgDeep` | `#0B0F1A` | Screen backgrounds |
| `BgBase` | `#12182A` | Secondary surfaces, inputs |
| `BgCard` | `#1A2035` | Cards, bottom sheets |
| `Purple` | `#A970FF` | Primary accent, CTAs |
| `Blue` | `#5B9CF6` | Secondary accent, sleep light stage |
| `Green` | `#34D399` | Low risk, positive delta |
| `Yellow` | `#FBBF24` | Medium risk, informational |
| `Red` | `#F87171` | High risk, stop action |
| `SleepDeep` | `#3B1F6E` | Deep sleep stage |
| `TextPrimary` | `#FFFFFF` | Body text, values |
| `TextSecondary` | `#A0A7B8` | Labels, captions |
| `TextMuted` | `#6B7280` | Placeholders, metadata |
| `Border` | `0x12FFFFFF` (4.7% white) | Card/input borders |

### Typography
| Style | Font | Size | Use |
|---|---|---|---|
| `displayLarge` | DM Serif Display | 48sp | Hero AHI / score number |
| `headlineMedium` | DM Serif Display | 24sp | Section headings |
| `titleMedium` | DM Sans SemiBold | 14sp | Card titles |
| `bodyLarge` | DM Sans | 15sp | Primary body |
| `bodyMedium` | DM Sans | 14sp | Secondary body |
| `labelSmall` | JetBrains Mono | 10sp | Tags, metadata, mono values |
| `labelMedium` | JetBrains Mono | 11sp | Interactive labels |

### Layout Conventions
- Screen horizontal padding: `20.dp`
- Screen top padding: `24.dp`
- Card corner radius: `14.dp`
- Card border: `1.dp Border`
- Card internal padding: `16.dp`
- Spacing between cards: `12.dp`
- Button height: `48–52.dp`, corner radius `12.dp`
- Bottom accent bar on `StatCard`: `3.dp`, full-width, matches card corner
