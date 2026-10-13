# SensorDash Android (native) - Phase 1 shell

Phase 1 only: a minimal Kotlin + Jetpack Compose app, plus a GitHub Actions workflow that builds an installable debug APK.
There are no gauges or sensors yet. The browser version of SensorDash lives in its own repository and is not touched by this one.

## Toolchain (checked October 11, 2026)

| Part | Version | Why |
|---|---|---|
| Android Gradle Plugin | 9.1.1 | Needs Gradle 9.3.1 and JDK 17; supports up to API 37 |
| Gradle | 9.3.1 | Minimum/default for AGP 9.1.1 |
| JDK | 17 (Temurin) | Minimum/default for AGP 9.x |
| Kotlin + Compose compiler plugin | 2.2.10 | Matches AGP 9.1.1 built-in Kotlin; Compose compiler uses the same version |
| Compose BOM | 2026.08.00 | Current stable BOM in the Android docs |
| compileSdk / targetSdk / minSdk | 36 / 35 / 26 | targetSdk 35 keeps the landscape lock working on tablets |

AGP 9.4.0 (September 2026, needs Gradle 9.6.0) already exists; I chose 9.1.1 because it is older and more settled.

## One-time setup (tablet only)

1. On github.com create a repository named `sensordash-android`. Tick **Add a README file** (Codespaces cannot open an empty repo). Public is simplest for downloads; private also works.
2. Open it: **Code > Codespaces > Create codespace on main**.
3. In the Codespaces file explorer, right-click the empty area > **Upload...** and pick `sensordash-android.zip`.
4. In the Codespaces terminal:
   ```
   unzip -o sensordash-android.zip && rm sensordash-android.zip
   git add -A && git commit -m "Phase 1: native shell and APK workflow" && git push origin HEAD:main
   ```
5. Open the repo's **Actions** tab. The "Build Android APK" run starts by itself. The first run is slow (it downloads Gradle and the Android SDK parts); later runs use the cache.
6. The first run generates and attempts to commit the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) to the repo. If the workflow reports that it could not push the wrapper, the APK build can still finish. Check the Actions log before running `git pull` in Codespaces.

Codespaces is only used for editing here. The build runs on GitHub Actions, so it does not use your Codespaces hours.

## Install on the OnePlus Pad

1. In the tablet browser (signed in to GitHub), open the repo > **Releases** > **SensorDash debug (latest build)**.
2. Tap **SensorDash-debug.apk** to download it. (Alternative: the Actions run's **Artifacts**, which is a zip.)
3. If Android says the browser may not install apps: tap **Settings** and allow **Install unknown apps** for that browser.
4. Open the downloaded APK and tap **Install**. If Play Protect warns about an unknown developer, choose **More details > Install anyway**.
5. Open SensorDash. Expected: a dark landscape screen with "SensorDash", "Native Android shell - Phase 1", and a line with your Android version and model.

## Signing (keeps secrets out of the repo)

Without secrets the APK is signed by a temporary key that changes on every build. That installs fine the first time, but a later build will not install over it (you must uninstall first). To fix that, create one permanent key and store it as GitHub secrets. The keystore file is never committed (`*.keystore` is git-ignored).

1. In the Codespaces terminal (it asks for a password and some name fields; remember the password):
   ```
   keytool -genkeypair -v -keystore sensordash.keystore -alias sensordash -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Print the key as one line, then select and copy the whole line:
   ```
   base64 -w0 sensordash.keystore
   ```
3. On github.com: repo **Settings > Secrets and variables > Actions > New repository secret**. Create four secrets:
   - `KEYSTORE_BASE64` = the line you copied
   - `KEYSTORE_PASSWORD` = the keystore password
   - `KEY_ALIAS` = `sensordash`
   - `KEY_PASSWORD` = the key password (the same as the keystore password if you were not asked for a separate one)
4. Delete the local file when done: `rm sensordash.keystore`. Keep a backup somewhere safe; if the key is lost, you must uninstall the app to install a build with a new key.
5. Uninstall the old SensorDash once, then install the next build. After that, updates install over the top.

## If the build fails

Open the failed run, expand the failing step, and send me the last ~40 lines of its log.

- A Kotlin/Compose version error mentioning "metadata" or "incompatible version": send the last 40 lines of the build log before changing versions.
- "SDK location not found" or licence errors: re-run the job; if it persists, send me the log.
- The wrapper push warning is harmless: the build still ran with the generated wrapper.

## What Phase 1 does not include

No gauges, no sensors, no permissions, no fullscreen or theme toggle. Those start in later phases, one phase at a time, each after your approval.
