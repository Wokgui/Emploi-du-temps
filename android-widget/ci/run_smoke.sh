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

# A tap must reach the paint-first handler promptly, before the heavy settings setup.
adb logcat -c
fast_start=$(date +%s%3N)
adb shell input tap 1010 145
fast_seen=0
for i in $(seq 1 30); do
  if adb logcat -d | grep -Fq "EDT_FAST_INPUT|settings|visual"; then
    fast_seen=1
    break
  fi
  sleep 0.05
done
fast_end=$(date +%s%3N)
fast_ms=$((fast_end-fast_start))
echo "settings_visual_ms=${fast_ms}" | tee smoke/interaction-latency.txt
test "$fast_seen" -eq 1
test "$fast_ms" -le 900
sleep 1
test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
adb exec-out screencap -p > smoke/02-settings.png

# Long-session regression: repeatedly switch views without restarting the app.
# 6.39 could progressively slow down because every DOM mutation triggered a full rescan
# and independent navigation renders could queue up. The final tap must remain prompt.
adb shell input tap 995 240
sleep 0.2
adb logcat -c
for i in $(seq 1 12); do
  adb shell input tap 180 1840
  adb shell input tap 540 1840
  adb shell input tap 870 1840
done
sleep 1
assert_main_alive
adb logcat -d > smoke/interaction-stress-log.txt || true
grep -Fq "EDT_FAST_STATS|" smoke/interaction-stress-log.txt
assert_clean_log smoke/interaction-stress-log.txt

adb logcat -c
stress_start=$(date +%s%3N)
adb shell input tap 1010 145
stress_seen=0
for i in $(seq 1 30); do
  if adb logcat -d | grep -Fq "EDT_FAST_INPUT|settings|visual"; then
    stress_seen=1
    break
  fi
  sleep 0.05
done
stress_end=$(date +%s%3N)
stress_ms=$((stress_end-stress_start))
echo "settings_after_36_nav_taps_ms=${stress_ms}" | tee -a smoke/interaction-latency.txt
test "$stress_seen" -eq 1
test "$stress_ms" -le 900
sleep 0.5
assert_main_alive
adb exec-out screencap -p > smoke/02b-settings-after-stress.png

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
for i in $(seq 1 80); do
  if adb logcat -d | grep -Eq "EDT_IMPORT_REVIEW.*visible"; then
    break
  fi
  if [ "$i" -eq 80 ]; then
    adb logcat -d > smoke/import-review-not-visible.txt || true
    exit 1
  fi
  sleep 0.25
done
sleep 1
test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
adb shell dumpsys activity activities > smoke/activity-import-review.txt || true
grep -Fq "com.wokgui.schedulewidget/.ImportReviewPreviewActivity" smoke/activity-import-review.txt
adb logcat -d > smoke/logcat-import-review.txt || true
grep -Eq "EDT_IMPORT_REVIEW.*visible" smoke/logcat-import-review.txt
grep -Eq "EDT_LAZY_IMPORT.*ready=true" smoke/logcat-import-review.txt
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
