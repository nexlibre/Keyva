# Beta 1.0.0 release gate

Status: development, not a verified beta. Build, three privacy policy unit tests, and interactive IME smoke checks passed on Android 12 (API 31) and Android 13 (API 33) in [run 38843925323](https://github.com/nexlibre/Keyva/actions/runs/38843925323) on 2026-10-16 UTC (2026-10-15 Nicaragua). This does not verify real microphone input, Gemini or full traditional-keyboard regressions.

| Check | Evidence / status |
|---|---|
| Source and history import | Upstream HeliBoard main 0c60dffb03a4e2e7a37cb82210f5299f93c03979; GPL-3.0 and other license files retained |
| Privacy policy unit tests | PASS: API 31 Robolectric; reports attached to the linked run |
| APK compilation, package, signature | PASS: org.nexlibre.keyva, API 31, apksigner verified; development signing only. APK SHA256: 564bc8ff4c685a47840500584a403dd4243ca6b47df563dc1f116f165ef57afd |
| Install, IME enable/default, settings launch | PASS on both Android 12 and 13: install, default IME activation, settings launch; no AndroidRuntime fatal exception in the exercised flow |
| Actual keyboard touch and selection preview | PASS on both emulators: a physical-coordinate key touch inserted text; IA selection preview opened and Back closed it |\n| Numbers, symbols, languages, suggestions, correction and manual preview editing | Pending broader interactive/device testing |
| Clipboard, emoji, themes, height, vibration | Preserved upstream; pending regression testing |
| Speech permission, listen, stop, cancel, offline language support | Implemented; pending actual microphone/service testing |
| Gemini real request, cancellation, regional style | Pending a newly provisioned user key and online test; no old key reused |
| Undo, preview, field changes, sensitive fields | Policy tested by CI; preview opening/cancel by Back tested. Undo, stale field and sensitive-field UI integration checks pending |
| WhatsApp, Telegram, browser and editor transitions | Pending physical device testing |
| Network failures, no internet, battery, memory/latency | Pending device testing |

Never commit credentials. Configure new key on Android only. Gemini free tier cannot be inferred from a model name; use an unbilled project and check AI Studio quota. The app does not enable billing. It cannot prevent costs on a user-supplied billed project. No retries or fallback models are automatic.

## Manual acceptance
Enable Keyva in Android settings, set as input method. Test Spanish/English, symbols/numbers, selection and clipboard, themes (including pure black), switching apps and screen rotation. Select a message with Nicaraguan voseo; press IA, review cloud notice, correct, insert and immediately undo. Verify exact original. Repeat after moving cursor: stale results must not insert. Repeat in password, visible password, numeric PIN and private-mode editors: voice/AI must refuse. Test microphone denial, local voice language unavailable, cancel/back, stop and app switch during listening. Confirm audio/transcripts do not persist. Test no key, bad key, quota exhausted, offline and timeout without losing text. Test credentials after lock/reboot and removal. Verify only intentionally selected preview text reaches Gemini. Unknown sensitive fields depend on the host app's EditorInfo classification; avoid personal information even in ordinary fields.
