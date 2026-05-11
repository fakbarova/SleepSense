# SleepSense — 3-Day Demo Sprint

> Priority-ordered task list. Each task is self-contained with file paths, acceptance criteria, and context.
> Demo day in 3 days. Goal: every screen looks alive, core features work E2E, AI Report is the WOW moment.

**Current status (May 4):**
- ✅ Demo seed data flow implemented via Settings → About → Load demo data.
- ✅ Recording start/stop now saves a real `SleepRecordEntity`.
- ✅ Passive `SleepTrackingService` is enabled from `MainActivity`.
- ✅ Backend `/report` endpoint implemented in the existing Fastify server.
- ✅ Android AI Report screen, repository, ViewModel, navigation, and Dashboard entry point implemented.
- ✅ Verified with `npm run typecheck` and `./gradlew :app:compileDebugKotlin`.
- ⏳ ESP32 simulator/status chip and optional sharing/notification polish remain.

---

## DAY 1 — Make the app demo-ready

### 1.1 Seed Data Populator (CRITICAL) ✅ DONE

**What:** Create a `DemoDataSeeder` that populates Room DB with 14 days of realistic sleep data on first launch (or via hidden toggle in Settings).

**Files to create:**
- `app/src/main/java/com/circadianx/sleepsense/data/seed/DemoDataSeeder.kt`

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/viewmodel/SettingsViewModel.kt` — add `seedDemoData()` method
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/SettingsScreen.kt` — add hidden "Load demo data" button in About section (long-press on version text or just a visible dev button)

**What to seed:**
```kotlin
// 14 SleepRecordEntity entries (past 14 nights):
// - startTimeMs: stagger from 22:00–23:30 each night
// - endTimeMs: 6:00–7:30 next morning (6–8.5h duration)
// - sleepScore: vary between 58–96 (some bad nights, trending upward)
// - disturbanceCount: 0–6 (correlate inversely with score)

// 14 StepDayEntity entries (past 14 days):
// - steps: 3000–12000 (realistic variation, weekends lower)

// 5 ChallengeEntity entries:
// - Mix of "sleep", "exercise", "screen_time" categories
// - Some started 7 days ago, some 14 days ago

// 3 GroupChallengeEntity + 4 StoryEntity entries

// Mark some RoutineCompletionEntity entries for past 7 days
```

**Acceptance criteria:**
- [x] After seeding, Dashboard shows score ring with last night's score
- [x] Dashboard sparkline shows 7-day trend
- [x] History screen shows 14 sessions with color-coded scores
- [x] Steps screen shows today's count and 7-day chart
- [x] Chat AI can reference "your recent sleep scores" meaningfully

**Implementation notes:**
- Seed trigger is visible in Settings → About as `Load demo data`.
- Seeder also marks onboarding complete, sets a bedtime schedule, and stores demo goals.

---

### 1.2 Fix Recording → Save Sleep Record ✅ DONE

**What:** When user taps "Stop recording", persist a real `SleepRecordEntity` based on elapsed time and simulated disturbances.

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/viewmodel/DashboardViewModel.kt`
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/RecordingScreen.kt`
- `app/src/main/java/com/circadianx/sleepsense/navigation/NavGraph.kt` — share the Dashboard-scoped ViewModel with Recording

**Implementation:**
```kotlin
// In DashboardViewModel:
// - Add recordingStartMs: Long? state
// - startRecording() sets recordingStartMs = System.currentTimeMillis()
// - stopRecording() calculates:
//     duration = now - recordingStartMs
//     disturbances = count peaks where liveDbfs > thresholdDbfs during recording
//     score = calculateScore(durationMs, disturbanceCount)
//   Then inserts SleepRecordEntity into sleepRecordDao
//   Then resets recording state

// calculateScore formula:
// base = (durationHours / 8.0 * 80).coerceIn(0, 80)
// penalty = disturbanceCount * 4
// score = (base - penalty + random(0..10)).coerceIn(0, 100)
```

**Acceptance criteria:**
- [x] Tap "Record" on Dashboard → Recording screen shows timer + waveform
- [x] Tap "Stop recording" → navigates back to Dashboard
- [x] Dashboard immediately shows new score (latest sleep updated)
- [x] History screen shows the new recording entry

---

### 1.3 Enable Foreground Service (passive tracking) ✅ DONE

**What:** Uncomment the service start in `MainActivity.kt` so passive sleep detection works.

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/MainActivity.kt`

**Acceptance criteria:**
- [x] App shows persistent notification "SleepSense is tracking sleep"
- [x] Foreground service compiles for API 28+

**Implementation note:**
- Enabled `SleepTrackingService`.
- Kept `StepCounterService` disabled for demo safety because Android health foreground service/runtime permissions can add extra launch risk.

---

## DAY 2 — WOW Feature: AI Sleep Report

### 2.1 Backend `/report` endpoint ✅ DONE

**What:** Add a new endpoint that generates a structured sleep report using GPT-4.1-mini.

**Files to create/modify:**
- `backend/src/server.ts` — registered `POST /report`
- `backend/src/llm.ts` — added `SleepReportInput` and `SleepReportOutput`

**Endpoint spec:**
```
POST /report
Body: {
  "sleepRecords": [
    { "date": "2026-04-30", "score": 82, "durationMinutes": 465, "disturbances": 2 },
    ...
  ],
  "steps": [8200, 6500, 11000, ...],
  "goals": ["Improve sleep quality", "Build exercise habit"],
  "userName": "Shohjahon"
}

Response: {
  "weeklyScore": 78,
  "previousWeekScore": 72,
  "trend": "improving",
  "patterns": [
    "You sleep 42 minutes longer on weekends compared to weekdays",
    "Your worst nights correlate with days under 5000 steps",
    "Disturbances peak between 2:00–3:30 AM"
  ],
  "riskAssessment": "Your sleep fragmentation index is low. No signs of obstructive sleep apnea based on disturbance patterns.",
  "recommendations": [
    "Maintain your 23:00 bedtime — your scores are 15% higher when you sleep before midnight",
    "On low-step days, add a 20-minute evening walk to improve deep sleep",
    "Consider reducing screen time after 22:00 to reduce sleep onset latency"
  ],
  "highlights": {
    "bestNight": { "date": "2026-05-01", "score": 94, "note": "No disturbances, 7h 45m total" },
    "worstNight": { "date": "2026-04-28", "score": 58, "note": "5 disturbances, only 5h 20m" }
  }
}
```

**GPT prompt strategy:**
- [x] System prompt: "You are a sleep health analyst. Given the user's sleep data, generate a structured JSON report..."
- [x] Use `response_format: { type: "json_object" }` for reliable parsing
- [x] Include the user's goals so recommendations are personalized

---

### 2.2 Android Report Screen ✅ DONE

**What:** A new scrollable screen displaying the AI-generated report with beautiful card-based layout.

**Files to create:**
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/ReportScreen.kt`
- `app/src/main/java/com/circadianx/sleepsense/viewmodel/ReportViewModel.kt`
- `app/src/main/java/com/circadianx/sleepsense/data/network/ReportRepository.kt`

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/navigation/NavGraph.kt` — add `Screen.Report` route + composable
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/DashboardScreen.kt` — add "View Report" button or make it accessible from quick actions

**UI structure:**
```
SsTopBar(tag = "AI Analysis", title = "Weekly Report")

── Header card ──
  "Week of Apr 28 – May 3"
  Large score: 78/100 (with SsScoreRing)
  Trend badge: "↑ 8% vs last week" (green)

── Patterns section ──
  SectionLabel("PATTERNS DETECTED")
  3 pattern cards with icon + text

── Risk Assessment card ──
  Green/yellow/red border based on risk level
  Text from AI

── Recommendations section ──
  SectionLabel("PERSONALIZED TIPS")
  Numbered recommendation cards

── Highlights section ──
  Row: "Best night" card + "Worst night" card

── Share button ──
  "Share report" (optional: export as image/text)
```

**Acceptance criteria:**
- [x] Accessible from Dashboard (button or menu)
- [x] Shows loading state while AI generates
- [x] Displays all sections from backend response
- [x] Handles error state (backend offline)
- [x] Looks stunning — this is the demo centerpiece

---

### 2.3 Wire Report to Navigation ✅ DONE

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/navigation/NavGraph.kt`

**Add:**
```kotlin
data object Report : Screen("report")

// In NavHost:
composable(Screen.Report.route) {
    ReportScreen()
}
```

**Add navigation trigger in DashboardScreen:**
- [x] Add `onOpenReport: () -> Unit = {}` parameter
- [x] Add `View AI Report` Dashboard button
- [x] Navigate to `Screen.Report`

---

## DAY 3 — Polish + Hardware Demo

### 3.1 ESP32 Simulator Mode ⏳ TODO

**What:** A fake BLE data generator that emulates the ESP32 sending packets, so the Bluetooth architecture can be demoed without hardware.

**Files to create:**
- `app/src/main/java/com/circadianx/sleepsense/data/bluetooth/Esp32Simulator.kt`

**Implementation:**
```kotlin
// Esp32Simulator:
// - Emits fake HardwarePacket.Heartbeat every 5 seconds
// - Emits fake HardwarePacket.Apnea every 30-60 seconds (random)
// - Updates btState to BtState.Connected
// - Runs as a coroutine flow, toggled via Settings or auto-detected when no real device

// The simulator should:
// - Insert ApneaEvents into Room DB
// - Update a live "device status" StateFlow
```

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/DashboardScreen.kt` — show connection chip: "ESP32 ✓" or "Simulated"

---

### 3.2 Connection Status Indicator ⏳ TODO

**What:** A small chip/badge in the Dashboard top bar showing ESP32 connection state.

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/circadianx/sleepsense/viewmodel/DashboardViewModel.kt` — expose `btState` from `BluetoothManager`

**UI:**
```
SsTopBar actions slot → small Chip:
  Connected: green dot + "ESP32" 
  Simulated: blue dot + "SIM"
  Disconnected: gray dot + "No device"
```

---

### 3.3 Fix RecordingScreen to use startRecording() ✅ DONE

**What:** Wire the "Record" button on Dashboard to call `viewModel.startRecording()` before navigating.

**Files to modify:**
- `app/src/main/java/com/circadianx/sleepsense/ui/screens/DashboardScreen.kt`
- `app/src/main/java/com/circadianx/sleepsense/viewmodel/DashboardViewModel.kt`
- `app/src/main/java/com/circadianx/sleepsense/navigation/NavGraph.kt`

**Acceptance criteria:**
- [x] Dashboard `Record` button calls `viewModel.startRecording()` before navigation.
- [x] Recording screen uses the same Dashboard-scoped ViewModel to stop and save the recording.

---

### 3.4 Demo Flow Script (practice this)

```
1. Open app → Dashboard shows score 82, sparkline trending up
2. Scroll down → "7h 12m total sleep, 2 disturbances, 14 nights tracked"
3. Tap "Ask AI" → type "How did I sleep this week?" → personalized answer
4. Back → tap "View Report" → AI generates weekly analysis (WOW MOMENT)
   - Show score comparison, patterns, recommendations
5. Bottom nav → Sleep tab → History with 14 color-coded sessions
   - Tap one → detail bottom sheet
6. Bottom nav → Habits → Toggle checkmarks, progress bar fills
7. Tap "Record" → show live waveform responding to voice
   - (Talk/breathe into mic to show it's real)
   - Stop → new entry appears in History
8. If ESP32 sim is ready: point to "ESP32 ✓" chip → "Hardware streams apnea events in real-time"
```

---

## OPTIONAL (if time remains)

### 4.1 Morning Summary Notification
- Sleep tracking service start is enabled
- Trigger `postMorningSummaryNotification()` after recording stops
- Demo: "Morning summary: Sleep score 82/100 · 2 disturbances · 7h 12m"

### 4.2 Share Report as Image
- Use `View.drawToBitmap()` or `ComposeView` snapshot
- Share via Android share sheet

### 4.3 Onboarding Skip for Demo
- If onboarding is already completed (seed data sets this), skip directly to Dashboard
- Ensure `OnboardingScreen` checks `state.onboardingCompleted` (it already does ✓)

---

## Key File Reference

| Purpose | Path |
|---------|------|
| Database | `app/.../data/db/SleepSenseDatabase.kt` |
| Sleep DAO | `app/.../data/local/db/dao/SleepRecordDao.kt` |
| Step DAO | `app/.../data/local/db/dao/StepDao.kt` |
| Bluetooth | `app/.../data/bluetooth/BluetoothManager.kt` |
| Chat backend | `app/.../data/network/ChatRepository.kt` |
| Report backend client | `app/.../data/network/ReportRepository.kt` |
| Dashboard VM | `app/.../viewmodel/DashboardViewModel.kt` |
| Report VM | `app/.../viewmodel/ReportViewModel.kt` |
| User prefs | `app/.../domain/repository/UserPreferencesRepository.kt` |
| Backend entry/routes | `backend/src/server.ts` |
| Backend LLM schemas | `backend/src/llm.ts` |
| Nav graph | `app/.../navigation/NavGraph.kt` |
| Theme colors | `app/.../ui/theme/Color.kt` |
| Spacing | `app/.../ui/theme/Spacing.kt` |

---

## Backend Setup (for demo day)

```bash
cd backend
echo "OPENAI_API_KEY=sk-your-key-here" > .env
npm install
npm run dev   # http://0.0.0.0:8080
```

Phone `local.properties`:
```
BACKEND_URL=http://<mac-ip>:8080
```

Also add the Mac IP to `app/src/main/res/xml/network_security_config.xml`:
```xml
<domain includeSubdomains="false">192.168.x.x</domain>
```

---

## Success Criteria (demo day)

- [x] App opens directly to Dashboard (onboarding completed via seed)
- [x] Dashboard shows animated score ring + sparkline + stats
- [x] "Ask AI" returns personalized answers referencing user data
- [x] "View Report" shows AI-generated structured analysis
- [x] Recording screen responds to real mic input
- [x] Stop recording creates a new sleep entry
- [x] History shows 14+ entries with detail sheets
- [x] Habits checkmarks toggle with progress bar
- [ ] No crashes during 5-minute demo walkthrough
- [ ] Backend runs on laptop, phone connects over Wi-Fi
