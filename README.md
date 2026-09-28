# Dev Options Quick

A non-root Android utility that uses an AccessibilityService to automate the visible Settings UI:

1. Open About phone.
2. Tap Build number up to 7 times.
3. Open Developer Options.
4. Set Window animation scale, Transition animation scale, and Animator duration scale to 0.5x.

## Important

This app does **not** use root, ADB, or WRITE_SECURE_SETTINGS. A normal third-party app cannot silently write Android's protected global settings. The user must explicitly enable the app's Accessibility Service once.

The automation is intentionally based on visible UI text and therefore can vary between Pixel/AOSP, Samsung, Xiaomi/HyperOS, OnePlus/OxygenOS, etc.

## Build with GitHub Actions

Push the repository to GitHub. The workflow in .github/workflows/build.yml builds a debug APK. After the workflow completes, download DevOptionsQuick-debug from the workflow's Artifacts section.

## Local build

Install Android Studio with JDK 17 and Android SDK Platform 36, then run:

```bash
gradle :app:assembleDebug
```
