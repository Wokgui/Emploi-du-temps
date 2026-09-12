#!/usr/bin/env bash
set -euo pipefail

# Build a branch-only soak test from the complete stable regression suite. It avoids
# accessibility lookups for off-screen WebView controls and dismisses emulator launcher
# ANRs before measurements so system dialogs cannot be mistaken for app latency.
python3 - <<'PY'
from pathlib import Path
src=Path('android-widget/ci/run_smoke.sh').read_text()

needle='''assert_clean_log() {
  local file="$1"
  ! grep -E "AndroidRuntime.*Process: com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|NotFoundError)" "$file"
}
'''
insert=needle+'''
# Pixel Launcher can occasionally ANR while the emulator is CPU-bound during the first
# WebView injection. Dismiss only that system dialog; never hide an ANR from our app.
dismiss_launcher_anr() {
  adb shell uiautomator dump /sdcard/edt-system.xml >/dev/null 2>&1 || true
  adb pull /sdcard/edt-system.xml smoke/edt-system.xml >/dev/null 2>&1 || true
  if [ -f smoke/edt-system.xml ] && grep -Fq "Pixel Launcher isn't responding" smoke/edt-system.xml; then
    adb shell input tap 360 1085 || true
    sleep 0.8
  fi
}
'''
if needle not in src: raise SystemExit('assert_clean_log anchor not found')
src=src.replace(needle,insert,1)

old_measure='''measure_settings_latency() {
  local label="$1"
  adb logcat -c
  local started=$(date +%s%3N)
  adb shell input tap 1010 145
'''
new_measure='''measure_settings_latency() {
  local label="$1"
  dismiss_launcher_anr
  adb logcat -c
  local started=$(date +%s%3N)
  adb shell input tap 1010 145
'''
if old_measure not in src: raise SystemExit('measure anchor not found')
src=src.replace(old_measure,new_measure,1)

old_launch='''adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
sleep 1
assert_main_alive
'''
new_launch='''adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
sleep 1
dismiss_launcher_anr
assert_main_alive
'''
if old_launch not in src: raise SystemExit('launch anchor not found')
src=src.replace(old_launch,new_launch,1)

start=src.index('# 6.44 real-session regression.')
end=src.index('\ncapture_main week week',start)
new_block=r'''# 6.44 production real-session regression. Keep one WebView alive and repeatedly use
# the real course editor, Settings, navigation and background/foreground lifecycle paths.
adb shell input tap 880 1810
sleep 0.35
dismiss_launcher_anr
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-soak.txt || true
adb logcat -c
for i in $(seq 1 48); do
  adb shell input tap 880 1810
  sleep 0.10
  adb shell input tap 510 825
  sleep 0.12
  adb shell input tap 500 1080
  sleep 0.16
  adb shell input tap 540 390
  sleep 0.10
  adb shell input tap 540 105
  sleep 0.14

  adb shell input tap 1010 145
  sleep 0.18
  adb shell input tap 862 210
  sleep 0.14

  adb shell input tap 165 1810
  sleep 0.08
  adb shell input tap 540 1810
  sleep 0.08
  adb shell input tap 880 1810
  sleep 0.10

  if [ $((i % 8)) -eq 0 ]; then
    adb shell am start -W -a android.settings.SETTINGS >/dev/null 2>&1 || true
    sleep 0.40
    adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
    sleep 0.50
    dismiss_launcher_anr
    assert_main_alive
  fi
  if [ $((i % 12)) -eq 0 ]; then sleep 1.5; fi
done
sleep 2
assert_main_alive
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-after-soak.txt || true
adb logcat -d > smoke/interaction-real-session-log.txt || true
assert_clean_log smoke/interaction-real-session-log.txt

# FastInteraction is production code and gives us real recognized taps without the
# diagnostic MutationObservers/setter hooks used during the investigation.
editor_inputs=$(grep -c "EDT_FAST_INPUT|edit-course|visual" smoke/interaction-real-session-log.txt || true)
settings_inputs=$(grep -c "EDT_FAST_INPUT|settings|visual" smoke/interaction-real-session-log.txt || true)
nav_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-real-session-log.txt || true)
course_form_rewraps=$(grep -c "EDT_FAST_FORM_WRAP|form=courseForm" smoke/interaction-real-session-log.txt || true)
echo "real_editor_inputs=${editor_inputs}" | tee -a smoke/interaction-latency.txt
echo "real_settings_inputs=${settings_inputs}" | tee -a smoke/interaction-latency.txt
echo "real_navigation_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
echo "course_form_fast_rewraps_during_soak=${course_form_rewraps}" | tee -a smoke/interaction-latency.txt
test "$editor_inputs" -ge 25
test "$settings_inputs" -ge 25
test "$nav_inputs" -ge 90
test "$course_form_rewraps" -eq 0

# formWrapped may have a non-zero startup baseline, but it must stay constant throughout
# the soak. More than one observed value means some legacy layer replaced the form again.
form_wrapped_values=$(sed -n 's/.*EDT_FAST_STATS.*formWrapped=\([0-9][0-9]*\).*/\1/p' smoke/interaction-real-session-log.txt | sort -nu | tr '\n' ' ')
form_wrapped_unique=$(sed -n 's/.*EDT_FAST_STATS.*formWrapped=\([0-9][0-9]*\).*/\1/p' smoke/interaction-real-session-log.txt | sort -nu | wc -l | tr -d ' ')
echo "formWrapped_values=${form_wrapped_values}" | tee -a smoke/interaction-latency.txt
if [ "$form_wrapped_unique" -gt 0 ]; then test "$form_wrapped_unique" -eq 1; fi

# A moderate WebView cache increase is normal; reject runaway process growth.
before_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-before-soak.txt || true)
after_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-after-soak.txt || true)
if [ -n "$before_pss" ] && [ -n "$after_pss" ]; then
  pss_growth_kb=$((after_pss-before_pss))
  echo "pss_growth_kb=${pss_growth_kb}" | tee -a smoke/interaction-latency.txt
  test "$pss_growth_kb" -le 25600
fi

measure_settings_latency settings_after_real_editor_settings_session
sleep 0.5
adb exec-out screencap -p > smoke/02c-settings-after-real-session.png

# Validate the requested Add-course layout after the soak.
adb shell input tap 862 210
sleep 0.20
adb shell input tap 880 1810
sleep 0.20
tap_text "Ajouter un cours"
sleep 0.5
adb exec-out screencap -p > smoke/02d-course-editor-layout.png
adb shell input tap 540 105
sleep 0.2
assert_main_alive
'''
src=src[:start]+new_block+src[end:]
Path('/tmp/run_smoke_644_generated.sh').write_text(src)
PY
chmod +x /tmp/run_smoke_644_generated.sh
exec bash /tmp/run_smoke_644_generated.sh
