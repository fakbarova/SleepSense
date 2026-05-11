# SleepSense — Work Tracker

Status key: `[ ]` = todo · `[x]` = done

---

## Wiring & Integration

- [ ] **Phase 1 — Foreground services:** Uncomment service startup in `MainActivity.kt:24-25` once `SleepTrackingService` and `StepCounterService` are stable. Requires manifest foreground-service type declarations.
- [ ] **MicrophoneRecorder integration:** Wire `MicrophoneRecorder` into `DashboardViewModel` (TODO comment at top of ViewModel). `liveDbfs` StateFlow is already consumed by `RecordingScreen` but the recorder is never started/stopped.
- [ ] **Chat backend call:** Implement the Ktor/Retrofit network call in `ChatScreen.kt` (Phase 3 TODO on the "Ask" onClick). Backend endpoint `/chat` is designed; Android side is stub only.
- [ ] **Dashboard "Morning Summary" button:** Wire the `onClick` on `DashboardScreen.kt` (Phase 1 comment) to navigate to a Morning Summary screen or `HabitsScreen`.
- [ ] **ReportScreen route:** Add `Screen.Report` to `NavGraph.kt` and expose an entry point from Dashboard or HistoryScreen. `ReportScreen.kt` exists but is unreachable.
- [ ] **ESP32 Bluetooth apnea ingestion:** `BluetoothManager` parses `ApneaEvent` JSON but does not write to Room. Wire `BluetoothManager` → `ApneaEventDao` inside `SleepTrackingService`.
- [ ] **HealthConnect permission flow:** `HealthConnectManager` exists but the permission-request flow (rationale dialog → `ActivityResultContracts`) is not shown to users. Add a health permissions step to Onboarding or a prompt in the Steps screen.
- [ ] **StepCounterService → StepDao:** Verify the sensor listener in `StepCounterService` correctly writes `StepDayEntity` rows. Currently untested because the service is commented out.
- [ ] **RoutineReminderReceiver:** Wire `AlarmManager` scheduling to the bedtime / wake times set during onboarding so that reminder notifications fire at the correct times. Receiver exists but no alarm is scheduled.

---

## UI / UX

- [x] **5-tab bottom navigation:** Reduced from 9 tabs to 5 (Home · Sleep · Habits · Social · Profile). Done during UI refresh.
- [x] **Top app bars:** Added `SsTopBar` to every main screen.
- [x] **Empty state component (`SsEmptyState`):** Replaces emoji-only strings with icon + title + body + optional CTA button.
- [x] **Bottom sheet create flows:** Challenges and Social create forms moved to `ModalBottomSheet` triggered by FAB.
- [x] **Dashboard hero score ring:** `SsScoreRing` circular progress added above stat cards.
- [ ] **Pull-to-refresh:** Add `pullRefresh` / `PullToRefreshBox` on HistoryScreen, StepsScreen, and SocialScreen.
- [ ] **Skeleton loaders:** Show shimmer placeholder while Room `StateFlow` emits its first value (currently shows empty state or blank).
- [ ] **Recording entry point:** Add a prominent "Start Recording" FAB or button on the Dashboard that navigates to `RecordingScreen`.
- [ ] **Accessibility pass:** Run Android Accessibility Scanner on Dashboard, History, and Settings. Verify all icons have `contentDescription`, touch targets ≥ 48dp, and color-contrast ratios ≥ 4.5:1 (AA).

---

## Quality

- [ ] **Consolidate DAO packages:** `data/db/` (old) and `data/local/db/` (new) both exist. Migrate any remaining legacy DAOs (`ApneaEventDao`, `SleepSessionDao` at `data/db/`) into `data/local/db/dao/` and delete the old package. Update `SleepSenseDatabase` to a single authoritative source.
- [ ] **ViewModel unit tests:** No tests exist. Add `@Test` classes for at least `DashboardViewModel`, `HistoryViewModel`, and `ChallengesViewModel` using `kotlinx-coroutines-test` + `turbine`.
- [ ] **Room migration tests:** 10 schema migrations exist. Add instrumented migration tests using `MigrationTestHelper` to prevent silent data-loss regressions.
- [ ] **`strings.xml` extraction:** Most UI strings are hard-coded Kotlin literals. Extract to `res/values/strings.xml` for i18n readiness.
- [ ] **Google Maps API key:** Placeholder key in `AndroidManifest.xml`. Replace with a real key restricted to this app's package + SHA before release, or remove if Maps is unused.
- [ ] **ProGuard / R8 rules:** Verify `gson`, `room`, `hilt`, `vico`, and `coil` are covered by keep rules in `proguard-rules.pro`.

---

## Hardware / Embedded

- [ ] **ESP32 BT pairing UX:** No in-app flow guides the user through pairing with the ESP32 sensor. Add a "Connect sensor" card to the Dashboard or Settings with step-by-step BT pairing instructions and a connection-state indicator.
- [ ] **Connection-state indicator:** Show a persistent chip (Connected / Scanning / Disconnected) in the Dashboard app bar or as a sticky banner when `BluetoothManager` reports state changes.
- [ ] **Sensor hardware documentation:** Add a `hardware/README.md` describing the ESP32 firmware protocol, BT service UUID, characteristic format for `ApneaEvent`, and wiring diagram.

---

## Future Enhancements (backlog)

- [ ] Cloud sync / multi-device: sync `SleepRecordEntity` and challenges to a backend database so data survives device wipe.
- [ ] Sleep stage classification: use accelerometer + mic data to estimate Light / REM / Deep stages and populate `SleepStageBar` component (already exists in `ui/components/`).
- [ ] Weekly insights: auto-generate a weekly report card using average score, best/worst night, challenge progress, and step streak.
- [ ] Social backend: replace local-only `GroupChallengeEntity` / `StoryEntity` with a server-backed API so groups are shared between users.
- [ ] Widget: home-screen Glance widget showing today's sleep score and step count.
- [ ] Wear OS companion app.
