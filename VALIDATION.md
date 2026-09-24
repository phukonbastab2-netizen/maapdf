# V2 validation

- `assembleDebug assembleDebugAndroidTest lintDebug` passed for version 2.0.
- Eight Android instrumentation test groups passed on Android 12: merge/split, extraction/order, rotation/crop/stamps, password round-trip, JPEG/compression/repair, Office exports, permanent raster blackout, and form fill/flatten.
- Generated DOCX, XLSX and PPTX exports reopened with independent python-docx, openpyxl and python-pptx readers. Expected text and three slide images were present. This checks package readability, not full desktop-Office compatibility or source-layout fidelity.
- Main screen visually inspected at 720 × 1280. Maa's continuous 2D animation is active; tapping her pauses it for accessibility inspection.
- See FEATURES.md for the explicit advanced-feature gaps. No iLovePDF API, paid AI service or cloud document upload was used.

## Earlier V1 validation retained

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
