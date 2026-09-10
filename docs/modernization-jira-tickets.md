# Jira Work Items: Modernize EOL and Outdated Dependencies in COG-GTM/ZebraEntepriseServices

Target Jira project: **COG-GTM**
Repository: `COG-GTM/ZebraEntepriseServices` (branch `master`, verified at commit `0c475c7`)

All versions and line numbers below were re-verified against the build files at the commit above. Each ticket is self-contained and can be copy-pasted into Jira as-is.

Priority legend: **Highest** = formally end-of-life (no security or bug fixes available); **High** = unsupported/blocking Play Store or toolchain compatibility; **Medium** = outdated but still functional.

---

## Epic: Modernize EOL and outdated dependencies / build toolchain in COG-GTM/ZebraEntepriseServices

**Description**

Zebra Enterprise Services (ZES) is an Android app (`EnterpriseServices`) plus two library modules (`PrintConnectIntentsWrapper`, `datawedgeprofileintentswrapper`) that bridge HTTP REST / Chrome Intents to DataWedge and PrintConnect. The Gradle build files still reference the legacy Android Support Library (`com.android.support.*`, EOL since 2018 with the 28.0.0 release), the legacy support test runner, `minSdkVersion 16` (Android 4.1, EOL), `compileSdkVersion`/`targetSdkVersion 32`, AGP 7.1.1/7.2.1, Kotlin 1.6.10, Gradle 7.3.3, NanoHTTPD 2.3.1 (last release 2016), JUnit 4.12 (2014), and a vendored copy of Gson 2.8.5.

This epic groups the individual upgrade tickets below. Ticket ordering matters: ZES-1/ZES-2/ZES-3 (AndroidX migration) should land before ZES-5 and ZES-6, because AndroidX is required for current `compileSdkVersion` and AGP versions.

**Current state snapshot (verified)**

| File | Line | Current value |
|---|---|---|
| `build.gradle` | 4 | `kotlin_version = '1.6.10'` |
| `build.gradle` | 5 | `compose_version = '1.0.5'` (declared, unused by any module) |
| `build.gradle` | 14 | `com.android.tools.build:gradle:7.1.1` |
| `build.gradle` | 20-21 | `com.android.application` / `com.android.library` version `7.2.1` |
| `gradle/wrapper/gradle-wrapper.properties` | 6 | `gradle-7.3.3-all.zip` |
| `EnterpriseServices/build.gradle` | 6 | `compileSdkVersion 32` |
| `EnterpriseServices/build.gradle` | 10-11 | `minSdkVersion 16`, `targetSdkVersion 32` |
| `EnterpriseServices/build.gradle` | 15 | `testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"` |
| `EnterpriseServices/build.gradle` | 31 | `androidx.appcompat:appcompat:1.2.0` |
| `EnterpriseServices/build.gradle` | 32 | `com.android.support.constraint:constraint-layout:1.1.3` |
| `EnterpriseServices/build.gradle` | 33 | `junit:junit:4.12` |
| `EnterpriseServices/build.gradle` | 34-35 | `androidx.test.ext:junit:1.1.2`, `androidx.test.espresso:espresso-core:3.3.0` |
| `EnterpriseServices/build.gradle` | 38 | `org.nanohttpd:nanohttpd:2.3.1` |
| `PrintConnectIntentsWrapper/build.gradle` | 12 | `compileSdkVersion 32` |
| `PrintConnectIntentsWrapper/build.gradle` | 15-16 | `minSdkVersion 16`, `targetSdkVersion 32` |
| `PrintConnectIntentsWrapper/build.gradle` | 20 | `testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"` |
| `PrintConnectIntentsWrapper/build.gradle` | 36 | `com.android.support:appcompat-v7:27.1.1` |
| `PrintConnectIntentsWrapper/build.gradle` | 37 | `junit:junit:4.12` |
| `PrintConnectIntentsWrapper/build.gradle` | 38-39 | `com.android.support.test:runner:1.0.2`, `com.android.support.test.espresso:espresso-core:3.0.2` |
| `datawedgeprofileintentswrapper/build.gradle` | 12 | `compileSdkVersion 32` |
| `datawedgeprofileintentswrapper/build.gradle` | 15-16 | `minSdkVersion 16`, `targetSdkVersion 32` |
| `datawedgeprofileintentswrapper/build.gradle` | 20 | `testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"` |
| `datawedgeprofileintentswrapper/build.gradle` | 36 | `androidx.appcompat:appcompat:1.2.0` |
| `datawedgeprofileintentswrapper/build.gradle` | 37 | `junit:junit:4.12` |
| `datawedgeprofileintentswrapper/build.gradle` | 38-39 | `androidx.test.ext:junit:1.1.2`, `androidx.test.espresso:espresso-core:3.3.0` |
| `datawedgeprofileintentswrapper/src/main/java/com/google/gson285/` | - | 75 vendored Gson 2.8.5 source files (package `com.google.gson285`) |

Source-level legacy references (also verified):

- `PrintConnectIntentsWrapper/src/androidTest/java/com/zebra/printconnectintentswrapper/ExampleInstrumentedTest.java` lines 4-5: `import android.support.test.InstrumentationRegistry;`, `import android.support.test.runner.AndroidJUnit4;`
- `EnterpriseServices/src/androidTest/java/com/zebra/enterpriseservices/ExampleInstrumentedTest.java` lines 4-5: same imports
- `datawedgeprofileintentswrapper/src/androidTest/java/com/zebra/datawedgeprofileintents/ExampleInstrumentedTest.java` lines 4-5: same imports
- `EnterpriseServices/src/main/res/layout/activity_url_print.xml` line 2: `<android.support.constraint.ConstraintLayout ...>`

**Epic acceptance criteria**

- `./gradlew clean assembleDebug assembleRelease` succeeds for all three modules.
- `./gradlew test` and `./gradlew connectedAndroidTest` (on a Zebra device or emulator) pass.
- `grep -rn "com.android.support\|android.support" --include=*.gradle --include=*.java --include=*.kt --include=*.xml .` returns no matches (excluding `android:supportsRtl`, which is an unrelated manifest attribute).
- No module declares `minSdkVersion 16`, `compileSdkVersion 32`, or `targetSdkVersion 32`.
- App installs and the REST endpoints (`/scan`, `/print`) and Chrome Intents behave as before on a Zebra device running the minimum supported Android version.

---

## ZES-1 — Migrate PrintConnectIntentsWrapper from legacy Android Support Library to AndroidX in COG-GTM/ZebraEntepriseServices

**Priority:** Highest
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

`PrintConnectIntentsWrapper` is the only module still depending on the legacy Android Support Library at runtime. `com.android.support:appcompat-v7:27.1.1` and the `com.android.support.test.*` artifacts have been end-of-life since the Support Library 28.0.0 final release (September 2018); they receive no bug or security fixes and cannot be combined with `compileSdkVersion >= 29` without Jetifier workarounds. The other two modules already use `androidx.appcompat:appcompat:1.2.0`, so this module is inconsistent with the rest of the project and blocks removing `android.enableJetifier` and raising AGP/compileSdk.

**Affected files and lines**

- `PrintConnectIntentsWrapper/build.gradle`
  - line 36: `implementation 'com.android.support:appcompat-v7:27.1.1'` -> `implementation 'androidx.appcompat:appcompat:1.2.0'` (match `EnterpriseServices/build.gradle` line 31 and `datawedgeprofileintentswrapper/build.gradle` line 36; upgrade all three together later if desired)
  - line 38: `androidTestImplementation 'com.android.support.test:runner:1.0.2'` -> `androidTestImplementation 'androidx.test.ext:junit:1.1.2'` (match the other modules) plus `androidTestImplementation 'androidx.test:runner:1.4.0'` if the runner is referenced directly
  - line 39: `androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'` -> `androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'`
- `PrintConnectIntentsWrapper/src/androidTest/java/com/zebra/printconnectintentswrapper/ExampleInstrumentedTest.java`
  - line 4: `import android.support.test.InstrumentationRegistry;` -> `import androidx.test.platform.app.InstrumentationRegistry;`
  - line 5: `import android.support.test.runner.AndroidJUnit4;` -> `import androidx.test.ext.junit.runners.AndroidJUnit4;`
- `gradle.properties`: confirm `android.useAndroidX=true`; `android.enableJetifier` can be removed once no `com.android.support` artifacts remain in any module.

**Acceptance criteria**

- `grep -rn "com.android.support" PrintConnectIntentsWrapper/` returns no matches.
- `grep -rn "android.support" PrintConnectIntentsWrapper/src` returns no matches.
- `./gradlew :printconnectintentswrapper:assembleRelease` succeeds.
- `./gradlew :printconnectintentswrapper:connectedAndroidTest` passes on a device/emulator.
- `EnterpriseServices` app builds and PrintConnect intents (`/print` REST endpoint, print Chrome Intent) still function on a Zebra device with PrintConnect installed.

---

## ZES-2 — Replace legacy support test runner with AndroidX test runner across all modules in COG-GTM/ZebraEntepriseServices

**Priority:** Highest
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

All three modules configure `testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"`. This class lives in the EOL `com.android.support.test:runner` artifact. `EnterpriseServices` and `datawedgeprofileintentswrapper` already depend on `androidx.test.ext:junit`/`androidx.test.espresso`, so their declared runner does not match their test dependencies and instrumented tests will fail to launch once Jetifier is disabled. The instrumented test sources in all three modules also still import `android.support.test.*`.

**Affected files and lines**

- `EnterpriseServices/build.gradle` line 15
- `PrintConnectIntentsWrapper/build.gradle` line 20
- `datawedgeprofileintentswrapper/build.gradle` line 20
  - In each: `testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"` -> `testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"`
- `EnterpriseServices/src/androidTest/java/com/zebra/enterpriseservices/ExampleInstrumentedTest.java` lines 4-5
- `datawedgeprofileintentswrapper/src/androidTest/java/com/zebra/datawedgeprofileintents/ExampleInstrumentedTest.java` lines 4-5
- `PrintConnectIntentsWrapper/src/androidTest/java/com/zebra/printconnectintentswrapper/ExampleInstrumentedTest.java` lines 4-5 (may be done in ZES-1)
  - `import android.support.test.InstrumentationRegistry;` -> `import androidx.test.platform.app.InstrumentationRegistry;`
  - `import android.support.test.runner.AndroidJUnit4;` -> `import androidx.test.ext.junit.runners.AndroidJUnit4;`
  - `InstrumentationRegistry.getTargetContext()` -> `InstrumentationRegistry.getInstrumentation().getTargetContext()` if used.

**Acceptance criteria**

- `grep -rn "android.support.test" .` returns no matches.
- `./gradlew connectedAndroidTest` launches and passes the `ExampleInstrumentedTest` in all three modules.
- `./gradlew assembleDebug` succeeds.

---

## ZES-3 — Migrate ConstraintLayout to AndroidX namespace in COG-GTM/ZebraEntepriseServices

**Priority:** Highest
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

`EnterpriseServices` depends on `com.android.support.constraint:constraint-layout:1.1.3`, the last legacy-namespace release of ConstraintLayout (2018, EOL). The layout `activity_url_print.xml` also references the legacy fully-qualified class name. The maintained artifact is `androidx.constraintlayout:constraintlayout` (2.1.x at time of writing).

**Affected files and lines**

- `EnterpriseServices/build.gradle` line 32: `implementation 'com.android.support.constraint:constraint-layout:1.1.3'` -> `implementation 'androidx.constraintlayout:constraintlayout:2.1.4'`
- `EnterpriseServices/src/main/res/layout/activity_url_print.xml` line 2 (and matching closing tag): `<android.support.constraint.ConstraintLayout` -> `<androidx.constraintlayout.widget.ConstraintLayout`
- Any Java references to `android.support.constraint.*` in `EnterpriseServices/src/main/java` (none found at verification time; re-check after change).

**Acceptance criteria**

- `grep -rn "android.support.constraint" EnterpriseServices/` returns no matches.
- `./gradlew :EnterpriseServices:assembleRelease` succeeds and lint reports no `UnknownId`/missing-class errors for the layout.
- The URL Print activity renders identically on a device (manual visual check).

---

## ZES-4 — Raise minSdkVersion off end-of-life Android 4.1 (API 16) in COG-GTM/ZebraEntepriseServices

**Priority:** Highest
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

All three modules declare `minSdkVersion 16` (Android 4.1 Jelly Bean, 2012). Google Play services and most AndroidX libraries dropped API 16-20 support (AndroidX now requires API 21+ for new releases; Google Play services requires 21+). No supported Zebra enterprise device ships below Android 8.1 (API 27), and Zebra's current LifeGuard-supported device portfolio runs Android 11+ (API 30+). Keeping API 16 forces legacy code paths, blocks AndroidX/AGP upgrades, and provides no customer value.

**Affected files and lines**

- `EnterpriseServices/build.gradle` line 10: `minSdkVersion 16`
- `PrintConnectIntentsWrapper/build.gradle` line 15: `minSdkVersion 16`
- `datawedgeprofileintentswrapper/build.gradle` line 15: `minSdkVersion 16`

**Work items**

1. Confirm with product/Zebra device support the oldest Android release on any device that must run ZES. Proposed value: `minSdkVersion 26` (Android 8.0) as a conservative floor for Zebra enterprise devices; `minSdkVersion 21` is the absolute minimum acceptable for current AndroidX.
2. Update all three modules to the agreed value (keep them identical).
3. Remove any `Build.VERSION.SDK_INT` branches that become dead code (search `EnterpriseServices/src/main/java` and the wrapper modules).
4. Document the compatibility impact and the new device floor in `README.md`.

**Acceptance criteria**

- No module declares `minSdkVersion` below 21; all three modules declare the same value.
- `./gradlew assembleRelease` and `./gradlew lint` succeed with no `NewApi` warnings.
- README documents the supported Android version range.
- App installs and REST/Intent flows work on a device at the new minimum API level.

---

## ZES-5 — Update compileSdkVersion/targetSdkVersion beyond Android 12 (API 32) in COG-GTM/ZebraEntepriseServices

**Priority:** High
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

All modules build against and target API 32 (Android 12L). Google Play requires new apps and updates to target API 34+ (Android 14) as of August 2024, with the requirement advancing annually; Android 12L no longer receives security updates. Raising compile/target SDK unlocks current AndroidX releases and exposes behaviour changes (foreground-service types, exported-component declarations, broadcast receiver export flags, notification permission) that ZES's `RESTHostService` and intent receivers must handle.

**Affected files and lines**

- `EnterpriseServices/build.gradle` line 6 (`compileSdkVersion 32`) and line 11 (`targetSdkVersion 32`)
- `PrintConnectIntentsWrapper/build.gradle` line 12 (`compileSdkVersion 32`) and line 16 (`targetSdkVersion 32`)
- `datawedgeprofileintentswrapper/build.gradle` line 12 (`compileSdkVersion 32`) and line 16 (`targetSdkVersion 32`)
- `EnterpriseServices/src/main/AndroidManifest.xml`: audit `<service>`, `<receiver>`, `<activity>` entries for `android:exported`, `foregroundServiceType`, and `POST_NOTIFICATIONS`/`FOREGROUND_SERVICE_*` permissions required by API 33/34.
- Runtime code registering broadcast receivers for DataWedge/PrintConnect results: add `RECEIVER_EXPORTED`/`RECEIVER_NOT_EXPORTED` flags (API 34 requirement).

**Dependencies:** ZES-6 (AGP must support the chosen compileSdk; AGP 8.x for API 34).

**Acceptance criteria**

- All modules declare `compileSdk 34` (or newer current Play requirement) and `targetSdk 34`; values are consistent across modules.
- `./gradlew assembleRelease` and `./gradlew lint` succeed.
- Manifest passes lint with no `ExportedService`/`ExportedReceiver` errors.
- On an Android 13+/14 Zebra device: the REST host service starts, `/scan` returns barcode data from DataWedge, `/print` sends a label via PrintConnect, and Chrome Intents work.

---

## ZES-6 — Upgrade build toolchain (AGP, Kotlin, Gradle) in COG-GTM/ZebraEntepriseServices

**Priority:** High
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

The build uses AGP 7.1.1 on the `buildscript` classpath while simultaneously declaring the `com.android.application`/`com.android.library` plugins at 7.2.1 in the `plugins {}` block (two conflicting AGP declarations), Kotlin 1.6.10 (Dec 2021, unsupported), an unused `compose_version 1.0.5`, and Gradle 7.3.3 (Dec 2021). AGP 7.x is out of support; Android Studio Ladybug+ and `compileSdk 34` require AGP 8.x, which requires Gradle 8.x and JDK 17. Kotlin 1.6 is incompatible with current AGP/Compose tooling.

**Affected files and lines**

- `build.gradle`
  - line 4: `kotlin_version = '1.6.10'` -> current stable (e.g. `1.9.24` or `2.0.x`)
  - line 5: `compose_version = '1.0.5'` -> remove (no module applies Compose)
  - line 14: `classpath "com.android.tools.build:gradle:7.1.1"` -> remove; rely solely on the `plugins {}` block
  - line 15: `classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"` -> move to `plugins { id 'org.jetbrains.kotlin.android' version '<kotlin_version>' apply false }` (or remove if no module uses Kotlin)
  - lines 20-21: plugin versions `7.2.1` -> `8.5.x` (or current stable)
- `gradle/wrapper/gradle-wrapper.properties` line 6: `gradle-7.3.3-all.zip` -> `gradle-8.7-bin.zip` (or the version required by the chosen AGP)
- `gradle.properties`: add `android.nonTransitiveRClass=true`, `android.nonFinalResIds` handling as required by AGP 8 defaults; confirm `android.useAndroidX=true`
- Module `build.gradle` files: AGP 8 requires `namespace` in each module's `android {}` block (replace `package` attribute in each `AndroidManifest.xml`), and `compileSdkVersion`/`targetSdkVersion`/`minSdkVersion` become `compileSdk`/`targetSdk`/`minSdk`.
- CI/dev docs: JDK 17 required.

**Dependencies:** ZES-1, ZES-2, ZES-3 (AGP 8 removes Jetifier-friendly paths and fails on legacy support artifacts).

**Acceptance criteria**

- Exactly one AGP version is declared (in `plugins {}`), matching the Gradle wrapper's supported range.
- `./gradlew --version` reports Gradle 8.x and JDK 17.
- `./gradlew clean assembleRelease test` succeeds with no deprecation warnings that will break in the next Gradle major (`./gradlew help --warning-mode all`).
- `compose_version` and the `buildscript` classpath AGP entry are removed.
- Project opens and syncs in the current stable Android Studio.

---

## ZES-7 — Replace or upgrade unmaintained NanoHTTPD embedded web server in COG-GTM/ZebraEntepriseServices

**Priority:** Medium
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

The core REST host (`RESTServiceWebServer` and the `RESTService*EndPoint` classes) is built on `org.nanohttpd:nanohttpd:2.3.1`, released in 2016. The project has had no release since and only sporadic commits; it does not support HTTP/2, has known issues with chunked encoding and header handling, and receives no security fixes. Because ZES exposes a local HTTP server to browsers on the device, the HTTP stack is a security-relevant component.

**Affected files**

- `EnterpriseServices/build.gradle` line 38: `api 'org.nanohttpd:nanohttpd:2.3.1'`
- `EnterpriseServices/src/main/java/com/zebra/enterpriseservices/RESTServiceWebServer.java`
- `EnterpriseServices/src/main/java/com/zebra/enterpriseservices/RESTServiceInterface.java`
- `EnterpriseServices/src/main/java/com/zebra/enterpriseservices/RESTServiceScanEndPoint.java`
- `EnterpriseServices/src/main/java/com/zebra/enterpriseservices/RESTServicePrintEndPoint.java`
- `README.md` REST API section (update if the server behaviour/headers change)

**Work items**

1. Spike: evaluate maintained embedded servers with small footprint and Android compatibility, e.g. Ktor server (CIO engine), Javalin/Jetty (heavier), or `com.sun.net.httpserver` via `org.nanohttpd` fork `nanohttpd-android`. Record the decision in this ticket.
2. If staying on NanoHTTPD, the `api` scope leaks NanoHTTPD types to consumers; change to `implementation` unless intentionally part of the public API.
3. Implement the replacement behind `RESTServiceInterface` so endpoint classes are unaffected.
4. Preserve existing routes, query parameters, CORS headers and JSON response format documented in `README.md` and exercised by `EnterpriseServices/SampleHTML`.

**Acceptance criteria**

- `org.nanohttpd:nanohttpd:2.3.1` is either removed or replaced by a dependency with a release in the last 12 months.
- All REST endpoints documented in `README.md` return identical responses (verified with the `SampleHTML` playground in Chrome on-device).
- `./gradlew assembleRelease test` succeeds.
- No `api` leakage of the HTTP library types unless documented.

---

## ZES-8 — Update JUnit 4.12 across all modules in COG-GTM/ZebraEntepriseServices

**Priority:** Medium
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

All modules use `junit:junit:4.12` (December 2014). JUnit 4.13.2 (2021) is the latest 4.x release and fixes several assertion and rule bugs; it is a drop-in replacement. Alternatively, the project can adopt JUnit 5 (Jupiter) via the `de.mannodermaus.android-junit5` plugin, but that requires AGP 8 (ZES-6) and test rewrites, so the recommended scope for this ticket is the 4.13.2 bump, with JUnit 5 as a follow-up if desired.

**Affected files and lines**

- `EnterpriseServices/build.gradle` line 33: `testImplementation 'junit:junit:4.12'` -> `testImplementation 'junit:junit:4.13.2'`
- `PrintConnectIntentsWrapper/build.gradle` line 37: same change
- `datawedgeprofileintentswrapper/build.gradle` line 37: same change
- Optionally align AndroidX test artifacts across modules at the same time: `androidx.test.ext:junit:1.1.5`, `androidx.test.espresso:espresso-core:3.5.1`.

**Acceptance criteria**

- `grep -rn "junit:junit:4.12" .` returns no matches; all modules declare the same JUnit version.
- `./gradlew test` passes for all modules.
- `./gradlew connectedAndroidTest` passes (if AndroidX test artifacts were bumped).

---

## ZES-9 — Replace vendored Gson 2.8.5 with a maintained Gradle dependency in COG-GTM/ZebraEntepriseServices

**Priority:** Medium
**Type:** Task
**Epic link:** Modernize EOL and outdated dependencies / build toolchain

**Description**

`datawedgeprofileintentswrapper` bundles a full copy of Gson 2.8.5 source (75 files) under `datawedgeprofileintentswrapper/src/main/java/com/google/gson285/`, renamed to the `com.google.gson285` package (the bundled `README.md` at lines 20 and 29 documents the original `com.google.code.gson:gson:2.8.5` coordinates). The `build.gradle` lines 40-41 show a commented-out `com.google.code.gson:gson:2.7` dependency, indicating the copy was vendored to avoid a version clash with consumers. Gson 2.8.5 (2018) predates fixes for CVE-2022-25647 (deserialization DoS, fixed in 2.8.9) and reflection-related crashes on newer Android/JDK versions. Vendored code cannot be scanned by dependency tooling (Dependabot, Snyk) and increases the library's size and maintenance burden.

**Affected files**

- `datawedgeprofileintentswrapper/src/main/java/com/google/gson285/**` (delete, 75 files)
- `datawedgeprofileintentswrapper/build.gradle` lines 40-41: replace the commented-out Gson lines with `implementation 'com.google.code.gson:gson:2.11.0'` (or current stable)
- Consumers of the vendored package in `datawedgeprofileintentswrapper/src/main/java/com/zebra/datawedgeprofileintents/`:
  - `DWProfileBaseSettings.java` lines 5-7 (`import com.google.gson285.Gson;`, `GsonBuilder`, `reflect.TypeToken`)
  - `DWProfileSetConfigSettings.java`
  - `DWProfileSwitchBarcodeParamsSettings.java`
  - Change `com.google.gson285.*` imports to `com.google.gson.*`.
- `datawedgeprofileintentswrapper/proguard-rules.pro` / `EnterpriseServices/proguard-rules.pro`: add Gson keep rules for the `DWProfile*Settings` model classes if `minifyEnabled true` (EnterpriseServices release build) strips field names.

**Alternative:** If the original clash concern with host apps is still valid for the published `com.zebra.datawedgeprofileintentswrapper` artifact, use the Gradle Shadow plugin to relocate `com.google.gson` at build time instead of committing relocated sources; or migrate to `kotlinx.serialization`/`org.json` which is part of the Android platform.

**Acceptance criteria**

- `datawedgeprofileintentswrapper/src/main/java/com/google/` directory no longer exists.
- `grep -rn "gson285" .` returns no matches.
- Gson is declared once in `datawedgeprofileintentswrapper/build.gradle` at a version >= 2.8.9 (CVE-2022-25647 fixed).
- `./gradlew :datawedgeprofileintentswrapper:assembleRelease` and `:EnterpriseServices:assembleRelease` (minified) succeed.
- DataWedge profile creation/switch via `/scan` REST endpoint and Intent wrapper works on a device (settings serialise/deserialise correctly).

---

## Suggested delivery order

1. ZES-1, ZES-2, ZES-3 (AndroidX migration; Highest, small, unblock everything else)
2. ZES-6 (toolchain), then ZES-5 (SDK levels) — these are tightly coupled and may be delivered in one PR
3. ZES-4 (minSdk) — requires product decision on device floor; can be delivered alongside ZES-5
4. ZES-8 (JUnit) — trivial, can be bundled with any of the above
5. ZES-9 (Gson) and ZES-7 (NanoHTTPD) — independent, require runtime verification on device
