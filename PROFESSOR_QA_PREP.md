# SleepSense — Professor Q&A Preparation

> Read once tonight. Skim again 30 minutes before presenting.
> Each answer is short on purpose — for spoken delivery, not reading aloud.

---

## SECTION 1 — Technical questions ("why this stack, why this choice")

### Q1. Why Android only? Why not iOS or cross-platform?
We're Android-first because (1) our target market is Central Asia and emerging markets where Android dominates, (2) Android's microphone + foreground-service APIs are mature for overnight recording, and (3) we wanted native performance, not a Flutter/React Native shell. iOS is on the roadmap once we validate retention.

### Q2. Why Jetpack Compose over XML views?
Compose is declarative, gives us state-driven UI, and our entire team already knew it from the course. It also let us build 9 screens in a single design system in days rather than weeks. The whole UI is reactive `StateFlow` → `collectAsStateWithLifecycle` — no manual UI updates anywhere.

### Q3. Why Room and not Realm or SQLDelight?
Room is the Google-recommended ORM, has KSP support, integrates with coroutines/`Flow` natively, and works offline by default. SQLDelight is more type-safe but had less Compose integration when we started. Realm has a heavier runtime.

### Q4. Why Hilt for dependency injection?
Hilt is built on Dagger, supported by Google, and has the lowest boilerplate for Android. It scoped our `MicrophoneRecorder`, `AuthSessionManager`, and `OkHttpClient` cleanly as singletons.

### Q5. Why Fastify on the backend, not Express or Nest.js?
Fastify is ~3× faster than Express, has built-in JSON schema validation, and good Zod integration. Nest is heavier than we needed for ~10 endpoints. Our backend is small and Fastify keeps it minimal.

### Q6. Why GPT-4.1-mini? Why not a local model?
Cost: GPT-4.1-mini is ~10× cheaper than GPT-4 with comparable quality on structured JSON output. We use `response_format: json_object` so the LLM returns parsable JSON every time. Local models (Llama, Phi-3) on a phone are too slow and quality is much lower for narrative summaries. We could swap to DeepSeek/Groq tomorrow if cost matters — the interface is OpenAI-compatible.

### Q7. How does the microphone-based detection actually work?
We sample audio at 44.1 kHz via Android's `AudioRecord` API, compute the loudness envelope (dBFS) every ~50ms, and emit a `Flow<Float>`. The waveform on screen is that flow. Detection is threshold-based: if dBFS exceeds the user-configured snore threshold (default −21 dBFS) for ≥5 seconds, we mark "snoring." If it then drops below −40 dBFS for ≥10 seconds, we mark a potential apnea event. **It's a heuristic, not ML — that's a v1 limitation we're honest about.**

### Q8. How accurate is the sleep score?
The score is a heuristic, not a clinical metric. Formula: `(durationHours/8 × 80) − (disturbances × 4)`, normalized to 0–100. It's a quality index, not a medical measurement. We label it that way deliberately. Real sleep staging requires EEG, which a phone mic can't do.

### Q9. What's stored locally, what's sent to the server?
Locally (Room): every sleep record, disturbance, habit completion, challenge, photo (encrypted), step count.
Sent to backend: aggregate metrics only — sleep score, duration, disturbance count, step counts, stated goals. **Never** raw audio, never photos, never PII beyond the user's first name.

### Q10. How do you handle the foreground service for overnight tracking?
`SleepTrackingService` is a foreground service with a persistent notification. Android won't kill it unless the system is critically low on memory. We start it via `startForegroundService()` in `MainActivity` and bind it to the bedtime schedule. Recording survives screen-off because the service holds a partial wake-lock.

### Q11. Battery impact?
We sample mic at 44.1 kHz but only compute envelopes — no FFT, no full audio storage. Approximate overnight cost: ~3–5% battery, similar to apps like Sleep Cycle. We don't keep the screen on. We don't transmit data overnight.

### Q12. Database migrations — what's your strategy?
For V1 we use destructive migrations (Room rebuilds the schema on version bump). That's acceptable during development. Production will use proper `Migration` objects per Room's documented pattern. We have ~15 entities and the schema is settling, so we'll lock it for v1.0 release.

### Q13. Why is the backend in TypeScript, not Kotlin?
Two reasons: (1) Node + Fastify boots in <1s and is easy to deploy on any cloud, (2) the OpenAI Node SDK is the most mature. A Kotlin (Ktor) backend would also work — we picked TS because deployment cost and AI tooling are better.

### Q14. How do you test?
Backend has a `server.test.ts` smoke test. The Android side is mostly UI-tested by hand for now — for a 4-week course project, automated UI tests weren't a priority. We type-check both sides (`tsc`, `kotlinc`) on every change.

### Q15. Authentication flow?
Google Sign-In on Android via Credential Manager → returns a Google ID token → app sends it to `/auth/verify` on our backend → backend calls Google's tokeninfo endpoint to validate → backend stores a hashed user ID derived from issuer + subject. All subsequent requests carry the ID token as a `Bearer` header. *(For tonight's demo, the auth gate is bypassed to keep the demo flow simple — the full path is wired and ready.)*

---

## SECTION 2 — Product questions ("why these features")

### Q16. Why these specific features and not [feature X]?
We built around one loop: **track → interpret → habit → track again**. Every feature serves one of those four. We deliberately *didn't* build smart alarms, sleep music libraries, or breathing exercises — those are well-served by other apps and would dilute the loop.

### Q17. What's the killer feature?
The **AI Weekly Report**. Most sleep apps show charts. We synthesize sleep + steps + goals into a written narrative comparing this week to last, identifying patterns ("worst nights match days under 5k steps"), and suggesting one concrete action. That's the moment users tell their friends about.

### Q18. How is your AI different from ChatGPT?
Two things. First, **context grounding**: every prompt to our LLM is wrapped with the user's actual data — last 14 nights of scores, step counts, completed habits, stated goals. ChatGPT alone would give generic advice. Second, **structured output**: we use `response_format: json_object` and Zod schema validation, so responses always have predictable fields the UI can render reliably.

### Q19. Why is there a habits engine in a sleep app?
Because tracking alone doesn't change behavior. Sleep apps that only track have <20% 30-day retention. Habits — like "no screens after 22:00" or "drink water before bed" — are what actually change next night's sleep. Our weekly report ties habit completion to score deltas so the user sees the cause-and-effect.

### Q20. Why social features in a sleep app?
Light social accountability — group challenges and shared stories — drives retention without being a "social network." Strava and Duolingo both grew on this exact pattern: you're more consistent when one person is watching. We deliberately kept it lightweight: groups are local-only in v1, no public feed, no comments.

### Q21. What about wearables — Apple Watch, Fitbit, Whoop?
We integrate with Google Health Connect, which reads from any compatible wearable. So if the user has a Fitbit or a Galaxy Watch, those steps automatically flow in. We don't *require* a wearable — that's our wedge — but we *use* one if available.

### Q22. What's the difference between Habits and Challenges?
**Habits** are daily checklists: routine items the user does every night/morning. Always-on. **Challenges** are time-bound goals: "sleep before midnight 5 of the next 7 days" — they have a duration and a success criterion. Habits build consistency; challenges push the user past plateaus.

### Q23. Progress photos in a sleep app — why?
Sleep affects appearance — eye bags, skin, posture. Some users (especially the wellness segment) want a visual log alongside the metrics. We made it optional, encrypted at rest, and keep it on-device only.

---

## SECTION 3 — Privacy, ethics, integrity ("the trap questions")

### Q24. Are you a medical device? Can you diagnose sleep apnea?
**No, and we're explicit about this on every screen.** SleepSense is informational. We provide a "low / medium / high" indicator based on disturbance frequency, but we always include a disclaimer: "this is not a medical diagnosis." If a user has serious concerns, the app suggests they consult a sleep specialist. We deliberately don't use clinical thresholds (like AHI ≥30) as diagnostic numbers.

### Q25. What happens to the audio recordings?
Audio is processed entirely on-device. We extract the loudness envelope (dBFS samples) in real time, then **discard the raw audio**. Nothing is uploaded. Nothing is saved to disk. The mic stream lives in memory for milliseconds.

### Q26. What about GDPR? What data do you have on users?
For tonight's demo: nothing leaves the device except aggregate metrics for the AI report, which is ephemeral (we cache responses for 5 minutes and discard). For production: Google Sign-In gives us email + name, which we hash to a stable `userId`. We don't sell data, don't run analytics SDKs, don't have advertising trackers. A future formal GDPR review would cover data export and deletion endpoints.

### Q27. The score formula is heuristic — isn't that misleading?
We label it a "Sleep Quality Index," not a clinical score. We also show the inputs (duration, disturbances) so the user can see how it's computed. The honest truth is: any sleep score from any app — Fitbit, Oura, Whoop — is a proprietary heuristic. The difference is we admit it.

### Q28. Couldn't false snoring detections cause anxiety?
Yes — that's a real risk with all consumer sleep tools. We mitigate it by: (1) a sensitivity slider so users can tune for their environment, (2) requiring sustained dB above threshold, not single peaks, (3) framing the AI report's risk note as "low/medium/high pattern, not a diagnosis." Long-term we'd add an on-device classifier to discriminate snoring from fan/AC noise.

### Q29. What if the AI hallucinates?
Two safeguards. First, structured output: the LLM must return JSON matching our Zod schema, so format errors fail loudly. Second, the LLM only ever sees aggregate numbers, not free text — so it can't invent dates or symptoms. The risk is in the *interpretation* (e.g., overstating a pattern). Our system prompt explicitly says "no medical diagnosis, no urgent claims." If we caught a problematic pattern, we'd add post-generation filters.

---

## SECTION 4 — Business / startup questions

### Q30. How would you monetize this?
Three potential paths. (1) **Freemium** — basic tracking + dashboard free, AI Weekly Report and Chat behind a $4–6/month subscription. (2) **B2B** — sell to corporate wellness programs that want sleep-as-productivity metrics for employees. (3) **Hardware partnership** — bundle with a low-cost sleep tracker. For v1, we'd start with freemium because retention is the bottleneck and free tier proves the loop works.

### Q31. Who are your competitors?
**Wearable-first**: Oura ($300 ring + $6/mo), Whoop ($30/mo bundled hardware), Fitbit. **Phone-only**: Sleep Cycle (most established), Pillow (iOS), SnoreLab (single-feature). **Our wedge**: AI narrative + habits + phone-only + Android-first emerging markets. We don't beat Oura on data quality — we beat them on price and accessibility.

### Q32. What's your unit economics?
LLM cost is the variable cost. Per active user per week: ~1 weekly report (~$0.005 with GPT-4.1-mini) + ~3 chat messages (~$0.001 each) = roughly $0.01/user/week. Mitigated by 5-minute response cache and per-user rate limiting. At freemium pricing, free tier costs us ~$0.04/user/month. Subscribers easily cover that.

### Q33. How would you acquire users?
For our primary segment (students), three channels: (1) university clubs and campus partnerships — direct, cheap, high trust, (2) TikTok/Instagram health-content creators in our region, (3) ASO on "sleep" + "AI" keywords. Pilot strategy: 20 student testers first, validate retention curves, then scale.

### Q34. What's the size of the opportunity?
Global sleep-tracking app market is ~$1.5B with ~15% CAGR. Our specific niche — phone-only, Android-first, emerging markets — is underserved. Even capturing 1% of Android sleep-app users in Central Asia (~50M smartphone users) would be a viable business.

### Q35. Why would users pay when there are free apps?
The free apps don't close the loop. They show charts. Our AI weekly report writes a *story* about your week — that's a different product category. We're more like a sleep coach than a sleep tracker.

### Q36. What's the moat? Anyone could copy this.
The technology isn't the moat — the **data flywheel** is. The more weeks of user data we have, the better the personalization. After 12 weeks, we can predict tomorrow's sleep from today's behavior. After 50 weeks, we can spot annual cycles. Competitors starting from scratch can't match that without users.

---

## SECTION 5 — Limitations & honesty (be ready)

### Q37. What doesn't work yet?
Honest answer:
- Sleep stage estimation is heuristic, not ML — labelled "estimated."
- ESP32 hardware path is designed but not wired in v1 — phone-only.
- iOS port is research-only.
- Login is bypassed for demo (full OAuth path is built and ready).
- Some screens (e.g., morning summary) have placeholder buttons.

### Q38. What's the biggest risk to the product?
Retention. Sleep apps are notoriously hard to keep users in past month one. Our three retention loops — daily morning summary, weekly AI report, social challenges — are the bet. If users don't open the app on day 8, the loop breaks. Pilot data will tell us if it works.

### Q39. What would you do differently if you started over?
Start with the AI Weekly Report. It's the differentiator. We spent equal time on every screen, but the report is the moment that makes users tell friends. Knowing that, we'd have built a thinner v1 with more polish on the report.

### Q40. What did the team learn?
Three things: (1) Compose + Hilt + Room is genuinely fast for an Android team to ship a polished app, (2) prompt engineering is a real engineering skill — getting reliable JSON out of an LLM took several iterations, (3) tracking ≠ behavior change — habits and the social loop are doing more work than the metrics.

---

## SECTION 6 — Demo failure recovery (if something breaks)

### If the AI Report fails to load
> "The backend depends on the OpenAI API and our local laptop being on the same network as the phone. Let me show you the cached version from earlier — and the architecture diagram so you can see how it would work."
Then: open History or Habits screens. The on-device features still work fully without the backend.

### If the recording mic shows nothing
> "Let me check the mic permission." Open Settings → Apps → SleepSense → Permissions. If denied, re-enable. If still nothing, switch to showing seeded History data.

### If the app crashes
> "Let me restart — this is a v1 demo build, expected to have some rough edges."
Restart, open to dashboard with seeded data.

### If Wi-Fi drops
> The on-device features work entirely offline — Dashboard, History, Habits, Challenges, Recording, Steps. Only Chat and AI Report need network. Run the offline portion of the demo and explain the AI report from screenshots.

---

## SECTION 7 — Team / personal questions

### Q41. Who built what?
*(Customize this — I don't know the actual split. Common ones:)*
- Shohjahon (you): backend, AI integration, system architecture.
- Muhammadjon: Android UI / Compose screens.
- Mardonali: data layer (Room, services, audio).
- Farangiz: UX, onboarding, design system.

### Q42. How long did this take?
Roughly 4 weeks of focused course work. The first week was architecture and stack decisions. Weeks 2–3 built the screens and data layer. Week 4 was AI integration, polish, and demo prep.

### Q43. What was hardest?
Two things. (1) Getting reliable structured JSON out of an LLM — early prompts produced inconsistent fields. We solved it with strict Zod schemas and explicit `response_format: json_object`. (2) The foreground service lifecycle — Android's battery optimization aggressively kills background work, and we had to test on multiple OEMs (Samsung, Xiaomi) to make recording survive overnight.

### Q44. What's next after this course?
Three priorities: (1) 20-user pilot to measure week-2 retention, (2) sleep-stage ML model trained on labeled audio, (3) iOS port research. We're treating this as a real product, not just a course submission.

---

## SECTION 8 — Quick reference cheat sheet

| Topic | One-line answer |
|---|---|
| Stack | Kotlin + Compose + Room + Hilt; Node + Fastify + OpenAI |
| AI model | GPT-4.1-mini, structured JSON, response cache, rate-limited |
| Data sent to server | Aggregate metrics only — never raw audio, never photos |
| Sleep score | Heuristic Sleep Quality Index, not a clinical metric |
| Medical claims | None — strictly informational, "not a medical device" |
| Killer feature | AI Weekly Report — narrative, not charts |
| Wedge | Phone-only, Android-first, emerging markets |
| Monetization | Freemium ($4–6/mo for AI report + chat) |
| Retention plan | Daily morning summary + weekly report + social challenges |
| Biggest risk | Week-2 retention — pilot will validate |
| Team size | 4 students (CircadianX) |
| Time to build | ~4 weeks |

---

## SECTION 9 — Phrases that buy time

When you don't know an answer immediately, use these:

- *"That's a great question — let me think about that for a moment."*
- *"That's exactly the kind of thing we'd validate in the 20-user pilot."*
- *"Honestly, that's a v2 problem — for v1 we deliberately scoped it out."*
- *"The architecture supports it; we just haven't built the UI yet."*
- *"Could you clarify what you mean by [specific term]? I want to make sure I answer the right question."*
- *"We considered three approaches — [A], [B], [C] — and chose [B] because [one reason]."*

Never say *"I don't know"* alone. Always pair it with: *"...but here's how I'd approach finding out."*

---

## SECTION 10 — Final 5-minute pre-demo checklist

```
☐ Backend running, /health responds from phone browser
☐ Phone on same Wi-Fi as laptop
☐ Phone in Do Not Disturb mode
☐ Phone brightness max
☐ App opens to Dashboard with seeded 14 nights
☐ AI Report loads in <15 seconds
☐ Recording mic shows live waveform when you speak
☐ Take a deep breath
☐ Smile
☐ Lead with the problem, not the product
```

---

**Final reminder:** the professor wants to see that you (1) understand your own architecture, (2) can defend your design choices, (3) are honest about limitations, and (4) have thought about the business beyond just the code. You have all four. Trust it.

Good luck.
