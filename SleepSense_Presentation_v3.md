# SleepSense — Presentation v3

> 10-minute pitch deck. 16 slides. ~35 seconds per slide.
> Format: copy each slide block into Google Slides / Canva / Gamma.
> Replace each `[IMAGE: ...]` placeholder with the described visual.

---

## Slide 1 — Title

**Headline:** SleepSense

**Subtitle:** A phone-only sleep companion that turns nightly data into stories, habits, and AI weekly reports.

**Tag line:** CircadianX Team · Course Demo Day

**Visual:** `[IMAGE: dark hero — purple "Sleep" + white "Sense" wordmark on a deep navy gradient, faint moon glyph in the corner]`

**Speaker notes (~25s):**
> Good [morning/afternoon]. We're CircadianX, and we built SleepSense — a sleep companion app for Android. Before I show you the product, I want to spend two minutes on the problem we're actually solving, because the product only matters if the problem is real.

---

## Slide 2 — The Hook

**Headline:** A third of the world is tired. They know it. They've tried apps. They're still tired.

**On-slide copy:**
- **1 in 3** adults sleeps less than 7 hours per night *(CDC, 2022)*
- **$680B/year** — global economic cost of sleep deprivation *(RAND Europe)*
- **<20%** of wellness-app users are still active after 30 days *(Statista)*

**Visual:** `[IMAGE: split screen — left, a person scrolling phone in bed at 1am; right, the same person yawning at a laptop the next morning]`

**Speaker notes (~35s):**
> This isn't a niche issue. The CDC says one in three adults gets less than seven hours of sleep. RAND estimates sleep deprivation costs the global economy almost seven hundred billion dollars a year. And the apps that are supposed to help? Less than one in five users sticks with them past the first month. So the problem isn't that people don't care about sleep. It's that the tools they have don't actually change anything.

---

## Slide 3 — Why Existing Apps Fail

**Headline:** Tracking isn't the same as changing.

**On-slide copy (table):**

| What current apps do | What's missing |
|---|---|
| Show charts and metrics | No interpretation — what does *82* mean? |
| Give generic tips | Not tied to *your* data |
| Live in their own silo | Habits, sleep, activity all separate apps |
| Require a $200+ wearable | Most students and emerging-market users don't own one |

**Visual:** `[IMAGE: phone screen mockup of a typical sleep app with a busy chart, overlaid with a question mark — "now what?"]`

**Speaker notes (~35s):**
> Today's sleep apps throw charts at users. They tell you your "deep sleep was 1h 47m" — but they don't say what to do about it. Their advice is generic: drink less coffee, sleep earlier. Habits live in another app. Activity in another. And most accurate trackers need a $200 wearable, which is a hard sell for students or anyone outside Western markets. Tracking has been solved. *Behavior change* hasn't.

---

## Slide 4 — Target Audience

**Headline:** Three people we built this for.

**On-slide copy (3 columns):**

| Tired Student (primary) | Burnt-out Professional | Worried Sleeper |
|---|---|---|
| **Age:** 18–24 | **Age:** 25–40 | **Age:** 30–55 |
| Irregular schedule, exam stress, 1am phone time | Long hours, anxiety, can't shut work off | Snores, partner complains, suspects mild apnea |
| **Hardware:** none | Maybe a watch | None — phone only |
| **Why us:** free, gamified, phone-only | AI weekly report explains why their work week ruins sleep | Snore detection + a risk hint *before* paying for a sleep clinic |
| **Trigger:** morning — "did I sleep enough to function?" | Sunday night — weekly review | Curiosity after a bad night |

**Visual:** `[IMAGE: three illustrated avatars side by side — student with textbook, professional with laptop, middle-aged person looking concerned at phone]`

**Speaker notes (~40s):**
> We built SleepSense for three people. The student — overworked, irregular schedule, phone in bed at 1am, no wearable, no budget. The young professional — burnt out, wants a Sunday-night summary that connects this week's bad sleep to too few steps and too much screen time. And the worried sleeper — someone who snores, whose partner has started complaining, who isn't ready to spend a thousand dollars on a sleep clinic but wants a hint. Our V1 wedge is the student — cheap to reach, easy to validate inside a university.

---

## Slide 5 — Market & Opportunity

**Headline:** A growing market with one underserved niche.

**On-slide copy:**
- Global sleep-tracker app market: **~$1.5B in 2024**, growing **~15% CAGR** *(Grand View Research)*
- ~50% of smartphone users have *tried* a wellness app — but **retention** is the real prize.
- **Underserved niche:** Android-first emerging markets (Central Asia, South Asia) where wearable adoption is low but smartphone adoption is universal.
- **Wedge:** phone-only, Android-first, free.

**Visual:** `[IMAGE: simple line chart of sleep app market 2020→2030 climbing; a circle highlighting "underserved emerging-market segment"]`

**Speaker notes (~30s):**
> The market itself is healthy — about 1.5 billion dollars and growing 15 percent a year. But it's crowded with wearable-first players like Whoop, Oura, Fitbit. Our wedge isn't a new wearable. It's targeting smartphone-only users in markets where wearables aren't realistic — starting in Uzbekistan and Central Asia, where Android dominates and clinical sleep tools barely exist.

---

## Slide 6 — Our Solution

**Headline:** Close the loop.

**On-slide copy:**
> SleepSense is the first phone-only app that connects **passive tracking → human insight → habits → next-night improvement.**

**Visual:** `[IMAGE: circular flow diagram — Track → Interpret → Habit → Track again. Purple arrows, dark background.]`

**Speaker notes (~25s):**
> So here's our thesis. Tracking on its own doesn't work. We close the loop: we passively track sleep with the phone microphone, we translate the data into a human-readable story, we tie habits to that story, and the habits change the next night's sleep. That's the loop, and it runs entirely on the device.

---

## Slide 7 — How We Solve Each Pain

**Headline:** One answer per pain.

**On-slide copy (table mirrors slide 3):**

| Pain | SleepSense answer |
|---|---|
| No interpretation | **AI Weekly Report** — narrative, not charts |
| Generic advice | **Context-injected chat** — uses *your* steps, score, goals |
| Disconnected habits | **Routines + Challenges** wired to sleep data |
| Hardware barrier | **Phone microphone only** — no wearable needed |

**Visual:** `[IMAGE: 4-row visual list, each row with a small icon — paragraph icon, chat bubble, checklist, smartphone]`

**Speaker notes (~30s):**
> Four pains, four answers. The AI weekly report turns metrics into a story. Chat is grounded in your real data, not generic. Routines and challenges turn the insight into action. And we did it all on the phone — zero hardware. Now let me show you what that actually looks like.

---

## Slide 8 — Tech Snapshot

**Headline:** Ship-quality engineering, not a prototype.

**On-slide copy (3 columns):**

**Android (frontend):**
- Kotlin + Jetpack Compose + Material 3
- Hilt DI · Room DB (15+ entities) · DataStore
- Foreground service for passive tracking
- AES-encrypted progress photos · CameraX · Health Connect

**Backend (Node + Fastify):**
- OAuth + per-user store
- LLM chat + weekly report (`/chat`, `/report`)
- LLM response **caching** + **rate limiting**
- Spotify wind-down playlist integration
- Walking-route nudges (`/route-suggest`)

**Audio & privacy:**
- Mic processed on-device
- Only loudness samples persisted
- Raw audio never leaves the phone

**Visual:** `[IMAGE: 3-column architecture diagram — phone (Android), cloud (Node/Fastify + LLM), and user-data layer with a lock icon]`

**Speaker notes (~40s):**
> A quick technical snapshot. The Android side is Kotlin and Jetpack Compose, with Hilt for DI and Room for local storage — fifteen-plus tables. On the backend, we're not just calling OpenAI — we have OAuth, a per-user store, response caching to keep cost down, rate limiting, and even Spotify integration for wind-down playlists. And critically: the microphone runs entirely on-device. We never upload raw audio. Only dB envelopes are persisted. That's the privacy promise that makes a sleep app okay to install.

---

## Slide 9 — Privacy & Integrity

**Headline:** What we promise, in plain English.

**On-slide copy:**
- **Audio stays on your phone.** We process it locally; only loudness samples are stored.
- **Photos are AES-encrypted at rest** (`PhotoCipher`).
- **LLM prompts** contain only aggregate metrics — never raw audio or photos.
- **Informational only.** SleepSense is not a medical device and does not diagnose sleep apnea.

**Visual:** `[IMAGE: 4-icon row — microphone with shield, lock on photo, brain with shield, doctor icon with a strikethrough]`

**Speaker notes (~25s):**
> Sleep apps are a trust product. So we're explicit. Audio is processed on the device. Progress photos are encrypted at rest. The LLM only ever sees aggregate metrics, never your raw data. And we make it very clear: this is informational, not diagnostic.

---

## Slide 10 — Demo: Onboarding & Dashboard

**Headline:** From install to insight in under 60 seconds.

> *Solves: hardware barrier, no interpretation*

**On-slide copy:**
- **3-step onboarding:** schedule → goals → permissions
- **Dashboard hero:** one number, one sentence, one timeline
- **"Last night, in three seconds":** score ring · night story · disturbance timeline · 7-day trend

**Visual:** `[IMAGE: phone mockup of Dashboard — large purple "82" score ring, "Good Sleep" label, sentence "Shorter than ideal, but your next best night is reachable", and a 7-day mini bar chart]`

**Speaker notes (~30s):**
> Onboarding is three taps: bedtime, goals, permissions. Then the dashboard. The hero number is a single sleep score. Below it, a one-sentence story. Below that, the night's timeline and a seven-day trend. The user gets value in under a minute — and that's the bar for retention.

---

## Slide 11 — Demo: Live Recording

**Headline:** A recording screen that doesn't ruin your sleep.

> *Solves: hardware barrier*

**On-slide copy:**
- Pulsing ambient ring (low-light friendly)
- Live waveform from phone mic
- On-device labels: *Quiet · Breathing · Snoring*
- Foreground-service guarded — survives screen-off

**Visual:** `[IMAGE: phone mockup of Recording screen — dark background, pulsing purple ring around a sleeping moon emoji, timer "07:23", small waveform, "Snoring detected (-32 dB)" label]`

**Speaker notes (~25s):**
> Recording is designed for a dark bedroom. A pulsing ring, a live waveform, and labels that change in real time as I breathe or speak. It runs in a foreground service so the OS doesn't kill it overnight. I'll demo this live — let me speak into the phone.

---

## Slide 12 — Demo: AI Weekly Report (THE WOW)

**Headline:** Charts don't change behavior. Stories do.

> *Solves: no interpretation, generic advice*

**On-slide copy:**
- Cross-references **14 nights × steps × your goals**
- Narrative summary + **+8% vs last week**
- Detected patterns ("Worst nights match days under 5k steps")
- Personalized recommendations
- Risk note with *strong* informational disclaimer

**Visual:** `[IMAGE: phone mockup of AI Weekly Report — header "Apr 28 – May 3", big "78 ↑8%", a Patterns card and a Recommendations card]`

**Speaker notes (~45s):**
> This is the feature we're most proud of. Every Sunday, our backend takes the last fourteen nights of sleep, the user's daily step counts, and their stated goals — and asks an LLM to write a weekly report. Not a dashboard. A *story*. It compares this week to last. It identifies real patterns — for example, that the user's worst nights happen on days under five thousand steps. And it ends with three personalized recommendations the user can act on tonight. This is the difference between data and insight.

---

## Slide 13 — Demo: History · Habits · Challenges

**Headline:** Insight only matters if it becomes habit.

> *Solves: disconnected habits*

**On-slide copy:**
- **History:** 7d / 30d / 90d range chips, color-coded weekly trend, tap a session for details
- **Routines:** evening + morning checklists, daily completion progress
- **Challenges:** time-bound goals ("Sleep before midnight, 5 of 7 nights")

**Visual:** `[IMAGE: 3 side-by-side phone mockups — History bar chart, Routines checklist with progress bar, Challenge card]`

**Speaker notes (~25s):**
> History gives the long view: seven, thirty, ninety days. Habits turn insight into a checklist — evening and morning routines. Challenges add time-bound commitments. This is where we earn the second week of retention.

---

## Slide 14 — Demo: AI Chat & Steps

**Headline:** Ask a real question. Get a real answer about *your* night.

> *Solves: generic advice*

**On-slide copy:**
- Quick prompt: *"Why did I sleep badly last night?"*
- Backend injects context: last night's score, disturbances, yesterday's steps, your goals
- Step data correlates with sleep quality in the report

**Visual:** `[IMAGE: phone mockup of Chat — user message "Why did I sleep badly last night?", AI response card "You had 5 disturbances and slept only 5h 20m. Your step count was 3,200 yesterday — typically correlates with lighter sleep."]`

**Speaker notes (~25s):**
> Most sleep-app chatbots are generic. Ours is grounded — every prompt is wrapped with the user's actual data. So "why did I sleep badly" gets answered with *your* numbers. Steps come from Health Connect; we use them as a context signal.

---

## Slide 15 — Usage & Retention Model

**Headline:** Why people come back tomorrow.

**On-slide copy (table):**

| Trigger | When | What user does | Time |
|---|---|---|---|
| Morning summary | 07:00 | Glance at score | 20s |
| Pre-sleep routine | 22:30 | Tap 4 checklist items | 30s |
| Weekly report | Sun 09:00 | Read the story, share | 2–3min |
| Bad-night chat | as-needed | Ask "why" | 1min |

**Total weekly engagement target:** ~20 minutes spread across 7 days *(Duolingo envelope)*.

**Three retention loops:**
- **Daily:** morning notification → see score → tap routine
- **Weekly:** AI report → share → set new challenge
- **Social:** group challenges → accountability

**Visual:** `[IMAGE: 24-hour clock face with 4 highlighted moments, plus a "weekly report" badge on Sunday]`

**Speaker notes (~40s):**
> The honest question for any wellness app is: will they open it on day eight? We have three retention loops. Daily — a morning notification with the night's score, and a wind-down nudge in the evening. Weekly — the AI report on Sunday, which is genuinely a different story every week. Social — shared challenges with friends. Total time-on-app is about twenty minutes a week, spread across seven days. That's the same envelope as Duolingo, and that's the model that works.

---

## Slide 16 — Roadmap & Ask

**Headline:** What's done, what's next, what we want from you.

**On-slide copy:**

**Shipped (V1):**
- Android app · 9 screens · 14-day demo data
- AI weekly report + chat (live backend)
- On-device audio · encrypted photos
- Habits · challenges · social · steps

**Next 4 weeks:**
- Login & cloud sync (the OAuth path is already built on the backend)
- Sleep-stage estimation (currently heuristic)
- iOS port — research

**Ask:**
- Feedback from the panel
- Access to ~20 student testers for a 2-week pilot

**Visual:** `[IMAGE: 3-column roadmap timeline — "Now" (shipped), "Next 4 weeks" (in-flight), "Ask" (pilot users)]`

**Speaker notes (~30s):**
> That's SleepSense. V1 ships today: nine screens, a working AI report, on-device audio, encrypted photos. The next four weeks are about login, sleep-stage estimation, and an iOS port. What we want from this room is feedback — and twenty student testers for a two-week pilot, so we can validate retention with real data. Thank you. I'm happy to take questions.

---

## Appendix — Q&A backup notes (don't put on slides)

- **"Isn't snore-from-mic noisy?"** Yes. We use a calibrated dB threshold and treat it as a v1 heuristic. Future work: on-device classifier.
- **"How is the score computed?"** Duration vs target + disturbance count, normalized 0–100. We label it "Sleep Quality Index" so it's clearly a heuristic, not a clinical measurement.
- **"What about Apple users?"** iOS is on the roadmap. Android-first is deliberate — our wedge is Android-heavy emerging markets.
- **"Is this a medical device?"** No. It's informational. We do not diagnose.
- **"What does the LLM see?"** Aggregate metrics only — sleep score, duration, disturbance count, step count, stated goals. Never raw audio. Never PII beyond a first name if the user provides one.
- **"Cost per user?"** LLM cost dominates. We mitigate with response caching (5-min TTL) and per-user rate limiting. Weekly report is the heavy call; we expect ~one report per user per week.
- **"Why phone-only?"** Wearables are a hardware adoption barrier in our target market. The mic is good enough for snore detection and disturbance counting, which is the data the AI report actually needs.
- **"What happens if backend is down?"** App still works offline using local Room storage. Dashboard, history, habits, challenges all run without network. Only chat and the weekly report require backend.
