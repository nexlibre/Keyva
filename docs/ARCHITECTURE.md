# Architecture and upstream audit

Audited source: HeliBorg/HeliBoard main commit 0c60dffb03a4e2e7a37cb82210f5299f93c03979. Imported with full available Git history into independent Keyva repository.

## License
Upstream top-level LICENSE is GPL-3.0. Files also carry Apache-2.0/GPL notices; artwork includes CC-BY-SA. Preserve notices and all license files and publish corresponding modified sources alongside binary distribution. No proprietary SwiftKey, Typeless or glide library was imported. Upstream AI_USAGE.md concerns contributions to their project; none were submitted there. Keyva modifications disclose AI assistance.

## Functional base
Single Android application module. Kotlin/Java IME in LatinIME; KeyboardSwitcher builds themed InputView and manages keyboard/symbol/emoji/clipboard surfaces. InputLogic handles composing text, suggestions and correction; native JNI dictionaries remain in place. Settings use Compose; layouts, locales and dictionaries are resource/asset based. Upstream minSdk 21 was raised to 31 (Android 12) per the final requirement; compile/target SDK adjusted to stable 36 because CI cannot resolve SDK 37. Android 13 is covered by planned CI as well.

Gradle wrapper 9.7.1, Android Gradle plugin 9.1.1, Kotlin 2.4.0, Java 17, NDK 28.0.13004108; actual resolution must be checked in CI. Dependencies include AndroidX core, RecyclerView, Autofill, ViewPager, Navigation, Compose Material3, Kotlin serialization, reorderable and colorpicker. The new AI layer adds no third-party SDK: platform SpeechRecognizer, Android Keystore, HTTPS and JSON only. CI generates the runtime dependency tree; this is an inventory, not a vulnerability audit. GitHub standard ubuntu-latest runners in a public repository are free under GitHub's published terms; no larger runners or paid services are configured.

## Integration boundaries
KeyboardSwitcher calls LatinIME.keyvaAttach on the existing InputView. AiController adds a toolbar inside the existing main keyboard frame; LatinIME includes its height in touchable insets. Traditional surfaces are not replaced. Session generation and InputConnection identity bind callbacks to the originating editor. Input start, view finish, recreation and service destruction cancel transient operations.

AiPolicy denies missing/unknown, non-text, password, visible password, web password, email and no-personalized-learning fields before text access. This relies on the host correctly identifying sensitive fields; it cannot detect arbitrary secrets inside an ordinary text box. AI reads selected text only and requires another action before sending. No automatic scanning or per-keystroke requests.

GeminiClient separates system instructions from user content, bounds input/output/response bytes, avoids logging request bodies and keys, disallows redirects, uses HTTPS and timeouts. A single worker executor performs requests off the UI thread. A stale/cancelled response is discarded. Model and quota selection are user controlled; no paid fallback or retries. Prompt separation mitigates instruction injection but cannot guarantee perfect model obedience; review is mandatory.

KeyStoreConfig stores AES-GCM encrypted key bytes and random IV in private preferences (the inherited app context uses device-protected storage), with an unlock-required Keystore key and an explicit UserManager unlock check. The AES key is non-exportable in Android Keystore; backup is disabled. Pre-unlock conventional keyboard remains available via inherited direct boot support. Only encrypted key bytes, IV and non-secret preferences are stored; no plaintext credential is written to preferences or source. FLAG_SECURE protects AI configuration and previews against ordinary screenshots; no protection against a compromised operating system is claimed.

SpeechRecognizer is created on the main thread. On-device engine is preferred when available, es-NI requested. Missing local support does not silently fall back to cloud. User must explicitly consent to the system recognition provider if local engine is absent. Local engine availability does not guarantee the es-NI language model is installed. A failure is surfaced without saving audio or switching providers. Dictation is bounded to 90 seconds and cancelled on session exit. Direct audio upload to Gemini is postponed; official Gemini audio support alone is insufficient to verify free quota, retention and latency for this application.

## Limitations to resolve before beta release
Physical microphone and fresh-key testing; interactive keyboard regression; stable release signing and retention; UI editing ergonomics; language fallback controls; dependency security audit; startup/memory/latency measurements. All CI outcomes and remaining acceptance tests belong in VALIDATION.md. Do not describe implementation as verified behavior before tests pass.
