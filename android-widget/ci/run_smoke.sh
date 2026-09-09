#!/usr/bin/env bash
set -e

mkdir -p smoke
adb install -r smoke-apk/emploi-du-temps-widget.apk
adb shell wm size 1080x1920
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true

wait_ui() {
  local label="$1"
  for i in $(seq 1 60); do
    if adb logcat -d | grep -Eq "EDT_UI_CHUNK.*complete count=[0-9]+"; then
      sleep 1
      return 0
    fi
    if [ "$i" -eq 60 ]; then
      adb logcat -d > "smoke/ui-ready-timeout-${label}.txt" || true
      return 1
    fi
    sleep 1
  done
}

assert_main_alive() {
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
  adb shell dumpsys activity activities > smoke/activity-current.txt || true
  grep -Fq "com.wokgui.schedulewidget/.MainActivity" smoke/activity-current.txt
}

assert_clean_log() {
  local file="$1"
  ! grep -E "AndroidRuntime.*Process: com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|NotFoundError)" "$file"
}

capture_main() {
  local label="$1"
  local mode="$2"
  local now="$3"
  local image="$4"
  adb logcat -c
  adb shell am force-stop com.wokgui.schedulewidget
  if [ -n "$now" ]; then
    adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode "$mode" --es test_now "$now" > "smoke/launch-${label}.txt"
  else
    adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode "$mode" > "smoke/launch-${label}.txt"
  fi
  wait_ui "$label"
  assert_main_alive
  adb exec-out screencap -p > "smoke/${image}"
  adb logcat -d > "smoke/logcat-${label}.txt" || true
  assert_clean_log "smoke/logcat-${label}.txt"
}

capture_main edit-cold edit "" 00-edit-cold.png

adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
sleep 1
assert_main_alive
adb exec-out screencap -p > smoke/01-edit.png
adb logcat -d > smoke/logcat-edit-full.txt || true
assert_clean_log smoke/logcat-edit-full.txt

adb shell input tap 1010 145
sleep 2
test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
adb exec-out screencap -p > smoke/02-settings.png

capture_main week week "" 03-week.png
capture_main before today 2026-09-10T07:45:00 04-before.png
capture_main active today 2026-09-10T08:30:00 05-active.png
capture_main lunch today 2026-09-10T12:30:00 06-lunch.png
capture_main gap today 2026-09-10T15:30:00 07-gap.png
capture_main after today 2026-09-10T17:30:00 08-after.png

adb logcat -c
adb shell am force-stop com.wokgui.schedulewidget
adb shell am start -W -n com.wokgui.schedulewidget/.ImportReviewPreviewActivity > smoke/launch-import-review.txt
wait_ui import-review
for i in $(seq 1 40); do
  if adb logcat -d | grep -Eq "EDT_IMPORT_REVIEW.*visible"; then
    break
  fi
  if [ "$i" -eq 40 ]; then
    adb logcat -d > smoke/import-review-not-visible.txt || true
    exit 1
  fi
  sleep 0.25
done
sleep 2
test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
adb shell dumpsys activity activities > smoke/activity-import-review.txt || true
grep -Fq "com.wokgui.schedulewidget/.ImportReviewPreviewActivity" smoke/activity-import-review.txt
adb logcat -d > smoke/logcat-import-review.txt || true
grep -Eq "EDT_IMPORT_REVIEW.*visible" smoke/logcat-import-review.txt
assert_clean_log smoke/logcat-import-review.txt
adb exec-out screencap -p > smoke/13-import-review.png

cat smoke/logcat-week.txt smoke/logcat-before.txt smoke/logcat-active.txt smoke/logcat-lunch.txt smoke/logcat-gap.txt smoke/logcat-after.txt smoke/logcat-import-review.txt > smoke/logcat-timetable.txt
grep -F "EDT_STARTUP_STATE" smoke/logcat-timetable.txt > smoke/startup-state.txt || true
python3 android-widget/ci/check_screenshots.py smoke

capture_widget() {
  local label="$1"
  local height="$2"
  local scenario="$3"
  local image="$4"
  adb shell am force-stop com.wokgui.schedulewidget
  adb shell am start -W -n com.wokgui.schedulewidget/.WidgetPreviewActivity --ei height_dp "$height" --ei width_dp 350 --es scenario "$scenario" > "smoke/launch-widget-${label}.txt"
  sleep 4
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
  adb shell dumpsys activity activities > "smoke/activity-widget-${label}.txt" || true
  grep -Fq "com.wokgui.schedulewidget/.WidgetPreviewActivity" "smoke/activity-widget-${label}.txt"
  adb exec-out screencap -p > "smoke/${image}"
}

capture_widget active 108 active 09-widget-active.png
capture_widget lunch 216 lunch 10-widget-lunch.png
capture_widget gap 216 gap 11-widget-gap.png
capture_widget after 108 after 12-widget-after.png

adb logcat -d > smoke/logcat-full.txt || true
assert_clean_log smoke/logcat-full.txt
