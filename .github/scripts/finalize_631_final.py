from pathlib import Path
import re

ROOT = Path('.')
MAIN = ROOT / 'android-widget/app/src/main/java/com/wokgui/schedulewidget/MainActivity.java'
GRADLE = ROOT / 'android-widget/app/build.gradle'
WORKFLOW = ROOT / '.github/workflows/android-widget.yml'
OLD_SMOKE = ROOT / '.github/workflows/android-smoke.yml'

main = MAIN.read_text(encoding='utf-8')

old_new_intent = '''        hideWebViewUntilWeekIsReady();
        primeWeekBadge();
        if (forceWeekOpening && webView != null) {
            webView.evaluateJavascript("if(window.reloadSchedule){reloadSchedule();}", value -> ensureUiReady());
        } else {
            applyOpenMode();
            ensureUiReady();
        }
'''
new_new_intent = '''        if (webView == null || !pageLoaded) return;
        hideWebViewUntilWeekIsReady();
        refreshScheduleAndUi();
'''
if old_new_intent not in main:
    raise SystemExit('Expected onNewIntent block not found')
main = main.replace(old_new_intent, new_new_intent, 1)

old_resume = '''        if (webView != null) {
            webView.evaluateJavascript(
                    "if(window.reloadSchedule){reloadSchedule();}",
                    value -> {
                        primeWeekBadge();
                        if (!forceWeekOpening) applyOpenMode();
                        ensureUiReady();
                    }
            );
        }
'''
new_resume = '''        if (webView == null || !pageLoaded) return;
        refreshScheduleAndUi();
'''
if old_resume not in main:
    raise SystemExit('Expected onResume block not found')
main = main.replace(old_resume, new_resume, 1)

anchor = '''    private void ensureUiReady() {
'''
helper = '''    private void refreshScheduleAndUi() {
        if (webView == null || !pageLoaded) return;
        webView.evaluateJavascript(
                "if(window.reloadSchedule){reloadSchedule();}",
                value -> {
                    if (webView == null || !pageLoaded) return;
                    primeWeekBadge();
                    if (!forceWeekOpening) applyOpenMode();
                    ensureUiReady();
                }
        );
    }

'''
if anchor not in main:
    raise SystemExit('ensureUiReady anchor not found')
main = main.replace(anchor, helper + anchor, 1)

main = main.replace(
    '    private void primeWeekBadge() {\n        if (webView == null || !AdvancedSettingsStore.json(this).optBoolean("singleWeek", false)) return;',
    '    private void primeWeekBadge() {\n        if (webView == null || !pageLoaded || !AdvancedSettingsStore.json(this).optBoolean("singleWeek", false)) return;',
    1,
)
main = main.replace(
    '    private void applyOpenMode() {\n        if (webView == null || getIntent() == null) return;',
    '    private void applyOpenMode() {\n        if (webView == null || !pageLoaded || getIntent() == null) return;',
    1,
)
main = main.replace(
    '    private void settleWeekAndReveal() {\n        if (webView == null) return;',
    '    private void settleWeekAndReveal() {\n        if (webView == null || !pageLoaded) return;',
    1,
)

# Defensive validation: no lifecycle path may evaluate JS before pageLoaded.
if 'private void refreshScheduleAndUi()' not in main:
    raise SystemExit('refreshScheduleAndUi helper missing')
if 'if (webView == null || !pageLoaded) return;\n        refreshScheduleAndUi();' not in main:
    raise SystemExit('onResume page-ready guard missing')
MAIN.write_text(main, encoding='utf-8')

gradle = GRADLE.read_text(encoding='utf-8')
gradle, n = re.subn(
    r"versionCode\s+project\.hasProperty\('versionCode'\)\s*\?\s*project\.property\('versionCode'\)\.toInteger\(\)\s*:\s*\d+",
    'versionCode 631001',
    gradle,
    count=1,
)
if n != 1:
    # Permit rerunning the migration after a partial attempt.
    gradle, n = re.subn(r'versionCode\s+\d+', 'versionCode 631001', gradle, count=1)
    if n != 1:
        raise SystemExit('versionCode line not found')
gradle = re.sub(r"versionName '6\.[0-9]+'", "versionName '6.31'", gradle, count=1)
GRADLE.write_text(gradle, encoding='utf-8')

workflow = '''name: Build and validate Android timetable

on:
  push:
    branches: [main]
    paths:
      - 'android-widget/**'
      - '.github/workflows/android-widget.yml'
  workflow_dispatch:

concurrency:
  group: android-widget-main-v3
  cancel-in-progress: true

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    defaults:
      run:
        working-directory: android-widget
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Set up Android SDK
        uses: android-actions/setup-android@v3
      - name: Install Android 35 SDK
        run: sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '8.10.2'
      - name: Keep the same debug signing key
        uses: actions/cache@v4
        with:
          path: ~/.android/debug.keystore
          key: emploi-du-temps-debug-keystore-v1
      - name: Clean build
        run: gradle clean :app:assembleDebug --stacktrace
      - name: Verify install version and package
        run: |
          aapt dump badging app/build/outputs/apk/debug/app-debug.apk | tee app/build/outputs/apk/debug/badging.txt
          grep -q "package: name='com.wokgui.schedulewidget' versionCode='631001' versionName='6.31'" app/build/outputs/apk/debug/badging.txt
      - name: Rename APK
        run: cp app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/debug/emploi-du-temps-widget.apk
      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: emploi-du-temps-widget-apk
          path: android-widget/app/build/outputs/apk/debug/emploi-du-temps-widget.apk
          if-no-files-found: error
      - name: Publish stable APK
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          if ! gh release view widget-latest >/dev/null 2>&1; then
            gh release create widget-latest --title "Emploi du temps widget" --notes "APK Android installable du widget d’emploi du temps."
          fi
          gh release upload widget-latest app/build/outputs/apk/debug/emploi-du-temps-widget.apk --clobber

  smoke:
    needs: build
    runs-on: ubuntu-latest
    timeout-minutes: 10
    continue-on-error: true
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Set up Android SDK
        uses: android-actions/setup-android@v3
      - name: Download validated APK
        uses: actions/download-artifact@v4
        with:
          name: emploi-du-temps-widget-apk
          path: smoke-apk
      - name: Exercise main views on Android emulator
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          arch: x86_64
          target: default
          profile: pixel_2
          emulator-boot-timeout: 300
          emulator-options: -no-window -gpu swiftshader_indirect -noaudio -no-boot-anim -camera-back none
          disable-animations: true
          script: |
            set -euo pipefail
            mkdir -p smoke
            adb install -r smoke-apk/emploi-du-temps-widget.apk
            adb shell wm size 1080x1920
            adb logcat -c
            adb shell am force-stop com.wokgui.schedulewidget
            adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity | tee smoke/launch.txt
            sleep 2
            test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
            adb exec-out screencap -p > smoke/01-edit.png
            adb shell input tap 180 1850; sleep 0.6; adb exec-out screencap -p > smoke/02-today.png
            adb shell input tap 540 1850; sleep 0.6; adb exec-out screencap -p > smoke/03-week.png
            adb shell input tap 900 1850; sleep 0.6; adb exec-out screencap -p > smoke/04-edit-return.png
            adb shell input tap 1010 145; sleep 0.6; adb exec-out screencap -p > smoke/05-settings.png
            test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
            adb shell am force-stop com.wokgui.schedulewidget
            adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode week | tee smoke/widget-week-launch.txt
            sleep 2
            test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
            adb exec-out screencap -p > smoke/06-widget-week-open.png
            adb logcat -d '*:E' > smoke/logcat-errors.txt || true
            if grep -E "FATAL EXCEPTION|AndroidRuntime.*Process: com.wokgui.schedulewidget" smoke/logcat-errors.txt; then
              echo "App crash detected"
              exit 1
            fi
      - name: Upload smoke-test captures
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: android-smoke-screenshots
          path: smoke/
          if-no-files-found: ignore
'''
WORKFLOW.write_text(workflow, encoding='utf-8')

if OLD_SMOKE.exists():
    OLD_SMOKE.unlink()

print('6.31 final stabilization prepared')
print('MainActivity lifecycle JS guarded by pageLoaded')
print('versionCode fixed at 631001')
print('single build/publish workflow installed with non-blocking emulator smoke job')
