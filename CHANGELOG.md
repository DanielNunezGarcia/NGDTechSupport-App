# Changelog

## FASE 10 - 2026-03-24

- Hardened Android `release` build by enabling R8/ProGuard (`isMinifyEnabled=true`) and resource shrinking (`isShrinkResources=true`).
- Added minimal and safe ProGuard rules for Kotlin metadata, coroutines-related warnings, and Firebase Firestore annotation mapping.
- Incremented app version to `versionCode=2` and `versionName=1.1.0`.
