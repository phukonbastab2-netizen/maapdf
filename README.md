# maapdf — Android document assistant V1

A small native Android app with large bilingual buttons. Android 8.0 or newer. No account, API key, advertising, analytics or paid AI dependency.

## V1 scope

| Format / feature | Supported |
|---|---|
| PDF | Native page viewing, previous/next/page command, save copy |
| TXT / CSV | UTF-8 text editing, confirm text replacement, save new copy |
| DOCX | Main-body text preview, explicit conversion to editable TXT copy |
| XLSX | Worksheet cell addresses and cached values; text preview, TXT copy |
| Import / camera | Image import to PDF; basic camera thumbnail capture to PDF |
| Voice | Android speech-recognition UI, Hindi locale, typed fallback |
| Read aloud | Text and Office previews through Android TTS |
| Commands | English/Hindi/Hinglish rules, optional HTTPS CPU backend |

No PDF text editing, OCR, document-layout reconstruction, formula calculation, Office round-trip editing, multi-page scanner, or guaranteed offline speech recognition. Camera quick capture uses the camera's thumbnail; import a full-resolution image for readable scans. Password-protected PDFs are unsupported. Maximum input file 20 MB; extracted Office XML 4 MB. Large documents may be slow. Office preview ignores images, headers, and complex formatting. CSV is edited as text, not a spreadsheet grid. Avoid reopening another file before saving your edits.

## Build

Install JDK 17 and Android SDK 35 with build-tools 35.0.0. Set `JAVA_HOME` and `ANDROID_HOME` (or create `local.properties` containing `sdk.dir=...`). Run `gradlew.bat assembleDebug` on Windows or `./gradlew assembleDebug` on Linux. APK: `app/build/outputs/apk/debug/app-debug.apk`.

This V1 download is a debug-signed test APK, not a Play Store production release. Future distribution should use an owner-controlled release signing key. Never commit keys. Android may ask you to permit installation from your browser. Only install from the owner's published page. SHA-256 is published beside the download.

## Lightning free CPU backend

The app works without a backend. This repository includes a zero-dependency Python intent parser. It is rule-based, not a trained LLM. Its response schema allows a future small local model to replace the parser without giving a model file access or direct edit permissions.

1. In your Lightning account choose a Studio explicitly marked **Free CPU**. Do not select a GPU or paid machine. Verify your account's free eligibility and storage use first.
2. Clone this repository, then run `python backend/server.py` (Python 3.10+).
3. Expose port 8000 using Lightning's HTTPS web-app/port feature. Check `<public-url>/health` returns `{"status":"ok","engine":"rules-cpu-v1"}`.
4. On the phone, Help & backend → enter that HTTPS base URL. The phone requests permission before sending an unrecognized command. Only that command is sent, never the file.

`POST /intent` with `{"text":"replace Suresh with Ramesh"}` returns `{"action":"replace","from":"Suresh","to":"Ramesh"}`. The phone still confirms replacements. Unknown actions are rejected. The endpoint is a minimal public stateless demo with request-size limits, not a multi-tenant production service. Deploy behind platform access/rate controls if used by a team. No backend is preconfigured or claimed deployed in the APK.

Lightning currently documents one free 4-CPU Studio; account eligibility, storage and service rules must be checked: https://api.lightning.ai/docs/platform/overview/faq/billing

## Privacy

Documents remain in phone storage and private temporary app cache. Only selected files are read via Android's file picker. Original files are never overwritten by the app. Save always uses the system's new-document picker. Android speech recognition may send audio to the phone's configured provider. TTS depends on installed languages. Optional backend sends command text only after confirmation. No embedded secrets. Uninstall removes app cache. Do not put sensitive personal information into voice commands if your speech provider is not approved for it.

## Validation

Run `python -m unittest discover -s backend`. Android builds and lint are recorded in the delivery notes. Real-device camera, speech recognition, TTS, file picker and OEM install behavior need device verification. An APK build alone does not establish those hardware-dependent behaviors.
