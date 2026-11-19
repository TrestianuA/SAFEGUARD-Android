# SAFEGUARD 0.7.3 — RAM refresh verification

## Change
The device health RAM card now has an **Actualizează RAM** button. Each press reads Android's `ActivityManager.getMemoryInfo()` again and updates available/total RAM and the `lowMemory` recommendation. No background polling, forced process termination, new permission, or stored telemetry was added. Existing layout colors, logos and APK signing configuration were not changed.

## Checks performed (2026-11-19)
- Inspected the current `MainActivity.kt` from the repository and the previous commit `069085a`.
- Confirmed the initial RAM snapshot was held in Compose `remember`, without manual refresh.
- Replaced it with observable Compose state and a manual refresh action, using the same Android API.
- No Gradle compilation, unit tests, APK signature verification or physical-device test was performed in this session. The change is **not** claimed to be build-verified.

## Next checks
1. Run `bash ./gradlew assembleDebug` and `bash ./gradlew testDebugUnitTest`; record the actual results.
2. Verify the RAM card's refresh button updates values on an Android device, including returning from Settings and changing memory conditions.
3. Confirm settings links, RO/EN copy, and accessibility behavior; add the animation guidance without changing system settings.
4. Before any installation over an existing SAFEGUARD version, compare the APK signing certificate fingerprints. Never expose private keys or disable Play Protect to force installation.

## Limitations
`availMem` is a system-wide snapshot, not per-app usage or a prediction of performance. `lowMemory` is Android's signal, not an antivirus finding. Values may remain the same across consecutive refreshes. Cache cleaning is manual only.

## 2026-11-19 — Manual animation guidance (commit a79e079)
- Reviewed the current `MainActivity.kt` and `.github/workflows/android-debug.yml` before editing.
- Added a bilingual RO/EN information card for Android animation settings to the device health section. The card describes the effect of reduced animations, explains that it does not increase physical RAM, and links to Android Accessibility settings.
- No change to Android system settings is performed by SAFEGUARD; no new permissions, logo changes or signing changes.
- Static review: the existing `helpEnglish` state is passed to `DeviceHealthCards` and controls the new copy. No compilation, tests or phone verification were run.
- Observed from the user's GitHub Actions screenshots: commit `cd1546b` failed after 14 seconds with exit code 22. This is consistent with, but does not prove, a failed `curl --fail` download in the workflow. Full job logs are needed for root-cause confirmation.
- The workflow currently fetches logo previews from external Canva URLs if logo resources are absent; this creates a build dependency on external URLs. Do not replace approved logos with generic assets. Prefer adding verified approved assets to source control and then removing the network dependency.
- Device test pending: verify the animation guide's accessibility path on Samsung Galaxy A13 / Android 14, and that RO/EN text changes when toggled.
