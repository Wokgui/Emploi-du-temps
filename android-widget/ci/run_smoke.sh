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

measure_settings_latency() {
  local label="$1"
  adb logcat -c
  local started=$(date +%s%3N)
  adb shell input tap 1010 145
  local seen=0
  for i in $(seq 1 30); do
    if adb logcat -d | grep -Fq "EDT_FAST_INPUT|settings|visual"; then
      seen=1
      break
    fi
    sleep 0.05
  done
  local ended=$(date +%s%3N)
  local elapsed=$((ended-started))
  echo "${label}_ms=${elapsed}" | tee -a smoke/interaction-latency.txt
  test "$seen" -eq 1
  test "$elapsed" -le 900
}

# Locate visible WebView controls by their accessibility text. If a control is lower in
# a scrollable sheet, scroll a little and retry. This lets the regression exercise the
# same editor buttons a real user touches instead of only fixed bottom-navigation taps.
tap_text() {
  local needle="$1"
  for attempt in $(seq 1 5); do
    adb shell uiautomator dump /sdcard/edt-ui.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-ui.xml smoke/edt-ui.xml >/dev/null 2>&1 || true
    local coords=""
    if [ -f smoke/edt-ui.xml ]; then
      coords=$(python3 - "$needle" <<'PY'
import re, sys, xml.etree.ElementTree as ET
needle=sys.argv[1].casefold()
try:
    root=ET.parse('smoke/edt-ui.xml').getroot()
except Exception:
    sys.exit(0)
for node in root.iter('node'):
    hay=((node.attrib.get('text') or '')+' '+(node.attrib.get('content-desc') or '')).casefold()
    if needle not in hay:
        continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.attrib.get('bounds',''))
    if not m:
        continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2>x1 and y2>y1:
        print((x1+x2)//2,(y1+y2)//2)
        break
PY
)
    fi
    if [ -n "$coords" ]; then
      adb shell input tap $coords
      return 0
    fi
    adb shell input swipe 540 1390 540 650 220
    sleep 0.12
  done
  echo "Could not find visible control containing: $needle" >&2
  adb exec-out screencap -p > "smoke/missing-control-${needle// /_}.png" || true
  return 1
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
grep -Eq "EDT_STARTUP_STATE\|inject-noop" smoke/logcat-edit-cold.txt
grep -E "EDT_UI_CHUNK.*complete count=[0-9]+ ms=[0-9]+" smoke/logcat-edit-cold.txt | tail -1 | tee smoke/ui-chunk-cold.txt

adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
sleep 1
assert_main_alive
adb exec-out screencap -p > smoke/01-edit.png
adb logcat -d > smoke/logcat-edit-full.txt || true
assert_clean_log smoke/logcat-edit-full.txt

: > smoke/interaction-latency.txt
measure_settings_latency settings_visual
sleep 0.6
adb exec-out screencap -p > smoke/02-settings.png

# Close Settings and retain the old navigation-only burst as a quick baseline.
adb shell input tap 862 210
sleep 0.3
adb logcat -c
for i in $(seq 1 12); do
  adb shell input tap 165 1810
  sleep 0.05
  adb shell input tap 465 1810
  sleep 0.05
  adb shell input tap 760 1810
  sleep 0.05
done
sleep 0.8
assert_main_alive
adb logcat -d > smoke/interaction-stress-log.txt || true
stress_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-stress-log.txt || true)
echo "navigation_inputs_seen=${stress_inputs}" | tee -a smoke/interaction-latency.txt
test "$stress_inputs" -ge 24
assert_clean_log smoke/interaction-stress-log.txt
measure_settings_latency settings_after_36_nav_taps
sleep 0.3
adb exec-out screencap -p > smoke/02b-settings-after-stress.png
adb shell input tap 862 210
sleep 0.3

# 6.44 real-session regression. The former 276-tap test was misleading: it mostly
# switched views and did not trigger the editor/settings refresh paths that used to
# reinstall wrappers. Keep ONE WebView alive and repeatedly do what a real user does:
# open/close the course editor, open/close Settings, and switch views in between.
adb shell input tap 760 1810
sleep 0.3
adb logcat -c
for i in $(seq 1 18); do
  tap_text "Ajouter un cours"
  sleep 0.12
  tap_text "Annuler"
  sleep 0.12

  adb shell input tap 1010 145
  sleep 0.14
  adb shell input tap 862 210
  sleep 0.12

  adb shell input tap 165 1810
  sleep 0.07
  adb shell input tap 465 1810
  sleep 0.07
  adb shell input tap 760 1810
  sleep 0.07

  if [ $((i % 3)) -eq 0 ]; then
    assert_main_alive
    sleep 0.25
  fi
done
sleep 1
assert_main_alive
adb logcat -d > smoke/interaction-real-session-log.txt || true
assert_clean_log smoke/interaction-real-session-log.txt
measure_settings_latency settings_after_real_editor_settings_session
sleep 0.4
adb exec-out screencap -p > smoke/02c-settings-after-real-session.png

# Also validate the requested course-editor layout after the long interaction session.
adb shell input tap 862 210
sleep 0.25
adb shell input tap 760 1810
sleep 0.25
tap_text "Ajouter un cours"
sleep 0.4
adb exec-out screencap -p > smoke/02d-course-editor-layout.png
tap_text "Annuler"
assert_main_alive

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
grep -Eq "EDT_IMPORT_PARSE\|count=6\|quality=(high|medium|low)\|headers=4" smoke/logcat-import-review.txt
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
  grep -Fq "com.wokgui.schedulewidget/.WidgetPreviewActivity" smoke/activity-widget-${label}.txt
  adb exec-out screencap -p > "smoke/${image}"
}

capture_widget active 108 active 09-widget-active.png
capture_widget lunch 216 lunch 10-widget-lunch.png
capture_widget gap 216 gap 11-widget-gap.png
capture_widget after 108 after 12-widget-after.png

adb logcat -d > smoke/logcat-full.txt || true
assert_clean_log smoke/logcat-full.txt
