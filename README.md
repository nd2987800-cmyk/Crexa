# Crexa - Modern Social Network App 🌟

Crexa is a full-featured, high-performance social networking Android application built with Kotlin, Jetpack Compose, Material Design 3, Room Database, and CameraX.

---

## 📱 Features
- **📸 Direct Camera Studio**: Instant photo captures, 60fps video recording, stories, and live broadcasting simulation.
- **📁 Universal Media Upload**: Dynamic permission handling with support for device storage and gallery picking for posts, reels, and stories.
- **🎥 Reels & Short Videos**: Vertical full-screen video feed with double-tap like, comments, sound discs, and share.
- **✨ Stories**: 24h story viewer with auto-advancing progress timers, pause-on-hold, and quick direct replies.
- **🤖 Crexa AI Engine**: Gemini-powered AI caption generator, hashtag suggester, and viral score analyzer.
- **⚡ 4GB RAM & Low-End Optimization**: Enabled hardware acceleration, large heap, and universal CPU architecture (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`).

---

## 🚀 How to Build & Run from GitHub / Downloaded ZIP

### Option 1: Using Android Studio
1. Extract the downloaded `Crexa.zip` file or clone the repo.
2. Open **Android Studio** (Koala / Ladybug or newer recommended).
3. Click **File -> Open...** and select the extracted folder.
4. Let Gradle sync dependencies automatically.
5. Click **Run (Shift + F10)** or select **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
6. The generated APK will be in `app/build/outputs/apk/debug/Crexa-app-debug.apk`.

### Option 2: Using Command Line (Terminal / CMD / PowerShell)
1. Open your terminal in the project root directory.
2. Run:
   - **Linux / macOS**: `./gradlew assembleDebug`
   - **Windows**: `gradlew.bat assembleDebug`
3. The APK will be ready at `app/build/outputs/apk/debug/Crexa-app-debug.apk`.

### Option 3: Automated GitHub Actions CI/CD
- Every `git push` triggers the workflow in `.github/workflows/build-apk.yml`.
- Go to your repository's **Actions** tab ➔ Click on the latest workflow run ➔ Download the **`Crexa-app-debug`** artifact.

---

## 📦 Requirements
- **JDK**: Java 17 or Java 21 (configured automatically via Gradle toolchain).
- **Android SDK**: Min SDK 24 (Android 7.0+), Target SDK 34 (Android 14).
