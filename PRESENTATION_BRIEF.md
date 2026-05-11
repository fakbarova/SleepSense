# SleepSense — Presentation Brief (for Teammates)

This document is designed to help you **present every feature** of SleepSense with a consistent story, demo flow, and ready-to-fill screenshot slots.


---

## 1. One-sentence pitch (opening slide)

**SleepSense is a mobile sleep companion that turns nightly sleep data into simple scores, trends, and actionable routines—plus an AI weekly report and chat—to help users build healthier sleep habits.**

---

## 2. What problem we solve (15–25 seconds)

- **Problem**: People sleep poorly, don’t understand why, and struggle to stay consistent with healthy routines.
- **Our approach**: Combine *tracking* + *habit systems* + *insights* + *AI explanations* in a single clean experience.
- **Outcome**: Users get a quick “last night summary,” a week trend, and concrete next steps.

---

## 3. Tech snapshot (1 slide)

- **Platform**: Android (Kotlin, Jetpack Compose)
- **Storage**: Room (local DB)
- **AI**: Backend-powered Chat + Weekly Report (`/chat`, `/report`)
- **Background**: Foreground `SleepTrackingService`
- **Input sources**: Microphone-based recording + demo dataset seeding for reliable presentation

Reference: `SleepSense/DEVELOPER_GUIDE.md`

---

## 4. Demo Flow (recommended order)

This order creates the strongest “WOW” moment early while still covering all features.

1. **Onboarding** (schedule → goals → permissions)
2. **Dashboard** (hero score + story + timeline)
3. **Recording** (live waveform + mic levels)
4. **AI Weekly Report** (loading → summary → patterns/tips)
5. **History** (week chart → sessions → bottom sheet details)
6. **Habits** (daily checklist)
7. **Challenges** (create challenge → active list)
8. **Steps**
9. **Social** (groups + stories)
10. **Chat**
11. **Progress Photos**
12. **Settings**

---

## 5. Feature-by-feature walkthrough (talk track + screenshots)

### 5.1 Onboarding (3 steps)

**Goal**: Set a bedtime schedule, choose goals, and optionally enable permissions for the best experience.

- **What to say**:
  - “We personalize tracking around your target bedtime and wake time.”
  - “Goals shape recommendations and AI insights.”
  - “Permissions are optional; the app still works without them.”

- **Demo steps**:
  - Pick bedtime/wake time
  - Select 1–3 goals
  - Show permission cards (Notifications / Usage access / Accessibility)

---

### 5.2 Dashboard (Home tab)

**Goal**: Show “last night at a glance” + 7-day trend + quick actions.

- **What to say**:
  - “The dashboard is built for speed: one hero score, a story, and quick actions.”
  - “We show a week trend so you can see if you’re improving, not just one night.”

- **Key UI elements**:
  - **Sleep score ring** (count-up + quality label)
  - **Night story** (human-readable summary)
  - **Night timeline strip** (visual disturbances)
  - Stats row: duration, disturbances, nights tracked
  - Actions: Record, Ask AI, View AI Report

- **Demo steps**:
  - If empty: use Settings → seed demo data (if available) so the dashboard becomes populated.
  - Point out the score + story + timeline and quick actions.

---

### 5.3 Recording (live session)

**Goal**: Show a live recording experience that feels “alive”: pulse ring, timer, waveform, live stats.

- **What to say**:
  - “Recording shows a calm state with ambient motion.”
  - “We visualize mic intensity as a waveform and label it Quiet/Breathing/Snoring.”

- **Demo steps**:
  - Tap **Record** → show Recording screen
  - Talk through waveform + live mic level
  - Tap **Stop recording** → explain it saves a sleep record


---

### 5.4 AI Weekly Report (the WOW moment)

**Goal**: Convert last 14 nights + steps + goals into a structured report.

- **What to say**:
  - “We generate a weekly score and compare it to the previous week.”
  - “We extract patterns, provide a risk-style note, and personalized tips.”
  - “This is informational—not a medical diagnosis.”

- **Demo steps**:
  - Open **View AI Report**
  - Show loading sequence (pattern discovery)
  - Scroll through narrative summary → patterns → risk → tips → highlights

---

### 5.5 History (Sleep tab)

**Goal**: Explore historical sessions + see a week chart + open detail bottom sheet.

- **What to say**:
  - “History is where users validate progress over time.”
  - “The weekly chart gives a trend; the session list gives details.”

- **Demo steps**:
  - Switch range chips (7d/30d/90d)
  - Tap a session → bottom sheet detail


---

### 5.6 Habits (daily routines)

**Goal**: Help users build consistent sleep behavior with checklists.

- **What to say**:
  - “Routines are the habit engine: simple checklists for tonight and morning.”
  - “Consistency is the point—small actions compound.”


---

### 5.7 Challenges

**Goal**: Commit to goals with durations and success criteria.

- **What to say**:
  - “Challenges turn intentions into time-bound commitments.”
  - “This also becomes a foundation for optional reward systems later.”

- **Demo steps**:
  - Open challenges from Habits
  - Show active challenge list
  - (Optional) create a new challenge


---

### 5.8 Steps

**Goal**: Show activity tracking context that correlates with sleep quality.

- **What to say**:
  - “Activity data helps explain sleep patterns—for example, low-step days often correlate with worse nights.”

---

### 5.9 Social

**Goal**: Encourage accountability via group challenges and shared stories.

- **What to say**:
  - “Social is designed for lightweight accountability: groups + short stories.”


---

### 5.10 Chat (AI assistant)

**Goal**: Let users ask questions in plain language with contextual sleep/habits/steps signals.

- **What to say**:
  - “Chat uses structured context (scores, durations, disturbances, steps, goals) so responses are personalized.”


---

### 5.11 Progress Photos

**Goal**: Private visual progress tracking; stored encrypted.

- **What to say**:
  - “Photos are encrypted at rest and only decrypted for display.”


---

### 5.12 Settings (Profile / Mic / Notifications / About)

**Goal**: Personalization and demo utilities (name, mic sensitivity, notifications, demo seed).

- **What to say**:
  - “Name is used in the AI report; mic sensitivity affects detection thresholds.”
  - “We can seed demo data for consistent presentation.”


---

## 6. Risks / limitations (1 slide, honest)

- **Not a medical device**: AI report is informational.
- **Backend dependency** for chat/report; demo requires server reachable.
- **Data model is demo-focused**: Room uses destructive migration during development.

---

## 7. Q&A-ready notes (quick answers)

- **Where does AI come from?** Backend endpoints: `/chat`, `/report`.\n+- **Where is data stored?** Locally in Room; preferences in DataStore.\n+- **Can we run without Android Studio?** Yes: Gradle + Android SDK CLI + device.\n+- **Can researchers use it?** For pilots/informal studies yes; formal research requires consent + secure export + governance.\n+
