# Beta 1.0.0 release gate

Status: development, not a verified beta. Build and privacy policy unit tests passed in [run 38843209804](https://github.com/nexlibre/Keyva/actions/runs/38843209804) on 2026-10-16 UTC (2026-10-15 Nicaragua). Emulator checks are still running at the time of this update. No release may be published based only on compilation.

| Check | Evidence / status |
|---|---|
| Source and history import | Upstream HeliBoard main 0c60dffb03a4e2e7a37cb82210f5299f93c03979; GPL-3.0 and other license files retained |
| Privacy policy unit tests | PASS: API 31 Robolectric; reports attached to the linked run |
| APK compilation, package, signature | PASS: org.nexlibre.keyva, API 31, apksigner verified; development signing only. APK SHA256: 21f8727b53d9af5b8768e292ba5b402765afcea803d5a17f758f02fc619a8cb4 |
| Install, IME enable/default, settings launch | CI emulator smoke checks Android 12 and 13 |
| Actual letters, numbers, symbols, languages, suggestions and correction | Pending interactive/device testing |
| Clipboard, emoji, themes, height, vibration | Preserved upstream; pending regression testing |
| Speech permission, listen, stop, cancel, offline language support | Implemented; pending actual microphone/service testing |
| Gemini real request, cancellation, regional style | Pending a newly provisioned user key and online test; no old key reused |
| Undo, preview, field changes, sensitive fields | Policy tested by CI; interactive integration checks pending |
| WhatsApp, Telegram, browser and editor transitions | Pending physical device testing |
| Network failures, no internet, battery, memory/latency | Pending device testing |

Never commit credentials. Configure new key on Android only. Gemini free tier cannot be inferred from a model name; use an unbilled project and check AI Studio quota. The app does not enable billing. It cannot prevent costs on a user-supplied billed project. No retries or fallback models are automatic.

## Manual acceptance
Enable Keyva in Android settings, set as input method. Test Spanish/English, symbols/numbers, selection and clipboard, themes (including pure black), switching apps and screen rotation. Select a message with Nicaraguan voseo; press IA, review cloud notice, correct, insert and immediately undo. Verify exact original. Repeat after moving cursor: stale results must not insert. Repeat in password, visible password, numeric PIN and private-mode editors: voice/AI must refuse. Test microphone denial, local voice language unavailable, cancel/back, stop and app switch during listening. Confirm audio/transcripts do not persist. Test no key, bad key, quota exhausted, offline and timeout without losing text. Test credentials after lock/reboot and removal. Verify only intentionally selected preview text reaches Gemini. Unknown sensitive fields depend on the host app's EditorInfo classification; avoid personal information even in ordinary fields.
