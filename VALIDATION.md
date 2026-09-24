# V1 validation

- Android Gradle 8.7.0 / Gradle 8.9 / JDK 17: `assembleDebug lintDebug` passed.
- Lint has no errors. Remaining warnings concern target SDK age and inline bilingual strings.
- APK signature verified with Android apksigner. Application label is `maapdf`, version 1.0, minimum Android 8.0, target API 35.
- Seven Java command-parser checks passed, covering English replacement, Hinglish replacement, Hindi replacement, Devanagari page number, page navigation, read-aloud intent and unknown-command rejection.
- Nine Python tests passed, covering commands, HTTP health, intent requests, malformed JSON and oversized requests.
- Installed and launched on a local Android 12 MuMu emulator.
- UI flow verified: typed document content, typed replacement command, cancel preserved `Suresh 5000`, confirm changed it to `Ramesh 5000`.
- Save-copy confirmation and Android document creation completed. Exported TXT was reopened outside the app and contained `Ramesh 5000`.
- Physical-device speech, camera and TTS behavior remain unverified. Office rendering fidelity, large-file handling and all PDF variants are not comprehensively tested.
- Lightning backend code is provided but no live Lightning deployment is configured or claimed.

The download is a debug-signed V1 test APK. Keep release-signing credentials out of source control. No paid service was started for this build.
