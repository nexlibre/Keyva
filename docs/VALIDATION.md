# Beta 1.0.0 release gate

**Development candidate; not a published or fully verified beta.** The [complete automated run 38892865907](https://github.com/nexlibre/Keyva/actions/runs/38892865907) passed for source `1115b56ad7d8e3a4429f678e40eb94e3a60b903d` on 2026-10-16 UTC.

| Check | Result and scope |
|---|---|
| Unit tests | PASS: nine Robolectric tests, including sensitive-field policy and simulated Gemini HTTP success, authentication/quota errors, cancellation, malformed/partial/oversized responses and input limits |
| Development compilation | PASS; org.nexlibre.keyva, minSdk 31, version 1.0.0-beta.1; debug signature verified |
| Optimized release compilation | PASS; unsigned candidate only, not a signed publication APK |
| Android 12, 13 and 15 installation | PASS: install, settings launch, IME enable/default selection |
| Conventional input with Wi-Fi/mobile data disabled | PASS: actual Keyva key touch inserted text |
| API key storage | PASS: synthetic placeholder encrypted, read back and deleted using Android Keystore; plaintext was not persisted in preferences |
| Preview editing and cancellation | PASS: Keyva keys edited the draft; cancelling left the host text unchanged |
| Insert and undo | PASS: exact edited-text insertion and restoration of the original selection |
| Sensitive/private editors | PASS: password and no-personalized-learning fields refused voice/AI; unit tests cover PIN, visible/web passwords and email variations |
| Denied microphone permission | PASS: routes to the user-controlled settings/permission flow |
| Dictation integration fixture | PASS: provider consent, ready, stop, result, cancel, transcript preview, insert and undo through Android SpeechRecognizer callbacks |
| Actual microphone and ASR | PENDING: the fixture never captures or recognizes audio; requires a supported device/service |
| Real Gemini and regional-style quality | PENDING: requires a new key configured only on Android and a real request; no exposed key was reused |
| Numbers, symbols, languages, dictionaries, clipboard, emoji, themes | Inherited; full interactive regression remains pending |
| Real WhatsApp, Telegram, browser/editor transitions; latency/battery | Pending physical-device acceptance |
| Permanent release signature | Not configured or verified through the available connector; see SIGNING.md |
| Publication | BLOCKED until real acceptance and permanent signing are complete; no beta release claimed |

Development APK SHA256: `937a952b2ca6a763d2d7f9efa3d54cafe164d67784b9c76fee51370c942132bb`.

## Test limitations
Gemini HTTP responses and the recognition provider are synthetic fixtures. Their success proves integration/error handling, not live Gemini correction quality or actual audio recognition. The recognition fixture is disabled by default, explicitly enabled by CI, and excluded from release builds. Android 15 tests use reported IME insets because edge-to-edge content does not necessarily resize.

## Remaining acceptance
Follow [ANDROID_ACCEPTANCE.md](ANDROID_ACCEPTANCE.md) from Android; no PC is required. Configure only a newly issued Gemini key in the app. Never submit a key in chat or source. Use an unbilled project and check Google quotas; Keyva does not enable billing and cannot prevent costs on a user-supplied billed project.

The release workflow checks the recorded real acceptance and passing CI, requires permanent signing credentials, builds/signs a non-debuggable candidate, verifies installation/default-IME activation, and only then publishes. The real-acceptance fields in RELEASE_VERIFICATION.json remain false.
