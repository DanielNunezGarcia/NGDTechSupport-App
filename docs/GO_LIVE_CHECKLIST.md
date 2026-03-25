# Go Live Checklist

## Pre-release Verification

- [ ] **Firebase configuration verified**
  - `google-services.json` contains correct project ID
  - Firebase console shows app registered with matching package name `com.example.ngdtechsupport`
  - `firebase.json` and `.firebaserc` point to correct project

- [ ] **API keys valid**
  - Google Maps / Places API keys have correct restrictions (SHA-1 + package)
  - Server-side API keys rotated if any were exposed in development
  - API quotas sufficient for expected production traffic

- [ ] **Release build successful**
  - `./gradlew assembleRelease` completes without errors
  - APK signed with production keystore (not debug)
  - ProGuard/R8 shrinking enabled (`isMinifyEnabled = true`)
  - Version code/name updated in `app/build.gradle.kts`

- [ ] **App signed with production keystore**
  - Release keystore stored securely (not in repo)
  - GitHub Secrets configured: `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`
  - CI pipeline produces signed release APK

## Firebase Services

- [ ] **Crashlytics and Performance Monitoring enabled**
  - `com.google.firebase.crashlytics` plugin applied in `app/build.gradle.kts`
  - `com.google.firebase.firebase-perf` plugin applied
  - Test crash sent and visible in Firebase console
  - `ENABLE_FIREBASE_MONITORING = true` in release build type

- [ ] **App Check configured**
  - Play Integrity provider enabled for release builds
  - Debug provider enabled for development (`firebase-appcheck-debug`)
  - App Check enforcement enabled for Firestore, Auth, and Storage

- [ ] **Firestore rules tested**
  - Rules deployed via `firebase deploy --only firestore:rules`
  - Security rules tested with emulator (`firebase emulators:start`)
  - Unauthorized access correctly denied in tests

## Quality Assurance

- [ ] **Unit tests passing**
  - `./gradlew test` passes with 0 failures
  - Code coverage report generated and reviewed
  - No new warnings in test output

- [ ] **Connected tests passing (on emulator/device)**
  - `./gradlew connectedAndroidTest` passes on API 34 emulator
  - Espresso UI tests verified on physical device
  - No flaky tests identified

## Documentation

- [ ] **Documentation up to date**
  - `README.md` reflects current setup and build instructions
  - `CHANGELOG.md` updated with release notes
  - `SECURITY_CONFIG.md` reviewed for accuracy
  - API documentation (if any) synced with code changes
