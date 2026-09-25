# maapdf — Android document assistant V3

A small native Android app with large bilingual buttons. Android 8.0 or newer. No account, paid API key, advertising or paid AI dependency. Google ML Kit sends performance/utilization metrics as described below.

## V3 scope

34 on-phone document tools, text-to-PDF export, large bilingual controls, and an original gently animated Maa caricature. Tools are grouped into Arrange, Convert, Write, Passwords & Forms, and Check & Reduce, and Read & Understand.

See [FEATURES.md](FEATURES.md) for each implemented feature and its limitations. This is not full iLovePDF feature parity. V3 adds English/Hindi OCR, searchable PDFs, key-sentence summaries, text translation, a new text-field tool, saved workflows and local HTML printing. Validated PDF/A, faithful Office conversions and generative AI summaries remain unimplemented. No paid service was started.

The main editor supports UTF-8 TXT/CSV, DOCX/XLSX text previews, basic camera/photo import, and PDF viewing/read-aloud for selectable text. Inputs to PDF tools are limited to 30 MB each, 20 files and 200 pages per job. Some PDFs/fonts/images are unsupported.

## Build

Install JDK 17 and Android SDK 35 with build-tools 35.0.0. Set `JAVA_HOME` and `ANDROID_HOME` (or create `local.properties` containing `sdk.dir=...`). Run `gradlew.bat assembleDebug` on Windows or `./gradlew assembleDebug` on Linux. APKs are split by CPU: `app-arm64-v8a-debug.apk` (most modern phones), `app-armeabi-v7a-debug.apk` (older 32-bit phones), and `app-x86_64-debug.apk` (test emulator). They are in `app/build/outputs/apk/debug/`.

This V3 download is a debug-signed test APK, not a Play Store production release. Future distribution should use an owner-controlled release signing key. Never commit keys. Android may ask you to permit installation from your browser. Only install from the owner's published page. SHA-256 is published beside the download.

## Lightning free CPU backend

The app works without a backend. This repository includes a zero-dependency Python intent parser. It is rule-based, not a trained LLM. Its response schema allows a future small local model to replace the parser without giving a model file access or direct edit permissions.

1. In your Lightning account choose a Studio explicitly marked **Free CPU**. Do not select a GPU or paid machine. Verify your account's free eligibility and storage use first.
2. Clone this repository, then run `python backend/server.py` (Python 3.10+).
3. Expose port 8000 using Lightning's HTTPS web-app/port feature. Check `<public-url>/health` returns `{"status":"ok","engine":"rules-cpu-v1"}`.
4. On the phone, Help & backend → enter that HTTPS base URL. The phone requests permission before sending an unrecognized command. Only that command is sent, never the file.

`POST /intent` with `{"text":"replace Suresh with Ramesh"}` returns `{"action":"replace","from":"Suresh","to":"Ramesh"}`. The phone still confirms replacements. Unknown actions are rejected. The endpoint is a minimal public stateless demo with request-size limits, not a multi-tenant production service. Deploy behind platform access/rate controls if used by a team. No backend is preconfigured or claimed deployed in the APK.

Lightning currently documents one free 4-CPU Studio; account eligibility, storage and service rules must be checked: https://api.lightning.ai/docs/platform/overview/faq/billing

## Privacy

Documents remain in phone storage and private temporary app cache. Only selected files are read via Android's file picker. Original files are never overwritten by the app. Save always uses the system's new-document picker. Android speech recognition may send audio to the phone's configured provider. TTS depends on installed languages. Optional backend sends command text only after confirmation. No embedded secrets. OCR and translation use Google ML Kit: content stays on-device, but models may be downloaded and performance/utilization metrics sent to Google. Translation is powered by Google Translate: https://cloud.google.com/translate. See https://developers.google.com/ml-kit/terms and licenses/MLKIT-NOTICE.md. Uninstall removes app cache. Do not put sensitive personal information into voice commands if your speech provider is not approved for it.

## Validation

Run `python -m unittest discover -s backend`. Android builds and lint are recorded in the delivery notes. Real-device camera, speech recognition, TTS, file picker and OEM install behavior need device verification. An APK build alone does not establish those hardware-dependent behaviors.
