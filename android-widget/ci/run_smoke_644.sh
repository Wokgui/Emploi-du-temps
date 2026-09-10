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
new_block=r'''# 6.44 real-session regression. The former 276-tap test was misleading because it
# mainly changed bottom tabs. Keep one WebView alive for a soak that repeatedly uses the
# real editor, Settings, navigation and background/foreground lifecycle paths.
adb shell input tap 880 1810
sleep 0.35
adb shell input swipe 540 560 540 1450 160 || true
sleep 0.2
dismiss_launcher_anr
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-soak.txt || true

# By here the prior 36-nav exercise has already produced diagnostic snapshots, so the
# runtime's last-submit identity is settled. Clear only logcat, not the WebView state.
adb logcat -c
for i in $(seq 1 48); do
  # Open an existing course row. The modal backdrop itself routes this to cancelEdit.
  adb shell input tap 500 900
  sleep 0.20
  adb shell input tap 540 105
  sleep 0.18

  # Real Settings open/close.
  adb shell input tap 1010 145
  sleep 0.22
  adb shell input tap 1000 245
  sleep 0.18

  # Real navigation in between editor/settings use.
  adb shell input tap 165 1810
  sleep 0.11
  adb shell input tap 540 1810
  sleep 0.11
  adb shell input tap 880 1810
  sleep 0.13

  # Every eight cycles, really leave the app and resume the same activity/WebView.
  if [ $((i % 8)) -eq 0 ]; then
    adb shell am start -W -a android.settings.SETTINGS >/dev/null 2>&1 || true
    sleep 0.45
    adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null
    sleep 0.55
    dismiss_launcher_anr
    assert_main_alive
  fi

  # Real users pause between bursts; this also exposes delayed timers/observers.
  if [ $((i % 12)) -eq 0 ]; then sleep 2; fi
done
sleep 3
assert_main_alive
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-after-soak.txt || true
adb logcat -d > smoke/interaction-real-session-log.txt || true
assert_clean_log smoke/interaction-real-session-log.txt

# Prove this was a real editor/settings session rather than misplaced coordinate taps.
editor_opens=$(grep -c "EDT_FAST_INPUT|edit-course|visual" smoke/interaction-real-session-log.txt || true)
settings_opens=$(grep -c "EDT_FAST_INPUT|settings|visual" smoke/interaction-real-session-log.txt || true)
nav_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-real-session-log.txt || true)
echo "real_editor_opens=${editor_opens}" | tee -a smoke/interaction-latency.txt
echo "real_settings_opens=${settings_opens}" | tee -a smoke/interaction-latency.txt
echo "real_navigation_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
test "$editor_opens" -ge 36
test "$settings_opens" -ge 36
test "$nav_inputs" -ge 100

# Unlike the previous baseline comparison, these counters detect actual changes that
# occurred DURING the soak. Repeated form rewrapping is the specific long-session bug.
runtime_snapshots=$(grep -c "EDT_RUNTIME_644" smoke/interaction-real-session-log.txt || true)
render_mutations=$(grep -c "EDT_RUNTIME_RENDER_MUTATION" smoke/interaction-real-session-log.txt || true)
submit_transitions=$(grep -c "EDT_RUNTIME_SUBMIT_TRANSITION" smoke/interaction-real-session-log.txt || true)
course_form_rewraps=$(grep -c "EDT_FAST_FORM_WRAP|form=courseForm" smoke/interaction-real-session-log.txt || true)
echo "runtime_snapshots=${runtime_snapshots}" | tee -a smoke/interaction-latency.txt
echo "render_mutations_during_soak=${render_mutations}" | tee -a smoke/interaction-latency.txt
echo "submit_transitions_during_soak=${submit_transitions}" | tee -a smoke/interaction-latency.txt
echo "course_form_fast_rewraps_during_soak=${course_form_rewraps}" | tee -a smoke/interaction-latency.txt
test "$runtime_snapshots" -ge 6
test "$render_mutations" -eq 0
test "$submit_transitions" -eq 0
test "$course_form_rewraps" -eq 0

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
adb shell input tap 1000 245
sleep 0.25
adb shell input tap 880 1810
sleep 0.25
tap_text "Ajouter un cours"
sleep 0.6
adb exec-out screencap -p > smoke/02d-course-editor-layout.png
# Close through the native backdrop/cancel path.
adb shell input tap 540 105
sleep 0.2
assert_main_alive
'''
src=src[:start]+new_block+src[end:]
Path('/tmp/run_smoke_644_generated.sh').write_text(src)
PY
chmod +x /tmp/run_smoke_644_generated.sh
exec bash /tmp/run_smoke_644_generated.sh
