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
new_block=r'''# 6.44 real-session regression. The former navigation-only stress was misleading.
# Keep one WebView alive and repeatedly use the real course editor, a descendant of its
# form, Settings, navigation and background/foreground lifecycle paths.
adb shell input tap 880 1810
sleep 0.35
dismiss_launcher_anr
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-soak.txt || true

# The diagnostic setter is installed after every production layer. Any course-form
# assignment below is therefore a genuine runtime rewrap, not normal startup assembly.
adb logcat -c
for i in $(seq 1 48); do
  # Reset to a deterministic edit state. Thursday has seven seeded courses in the smoke
  # profile, so this cannot silently turn into a tap on an empty-day placeholder.
  adb shell input tap 880 1810
  sleep 0.10
  adb shell input tap 510 825
  sleep 0.12

  # First Thursday course. Then touch neutral space inside the editor's <form>; this is
  # the exact path that used to make FastInteraction and FineTune wrap each other.
  adb shell input tap 500 1080
  sleep 0.16
  adb shell input tap 540 390
  sleep 0.10
  # Modal backdrop routes through the real cancelEdit handler.
  adb shell input tap 540 105
  sleep 0.14

  # Real Settings open/close. Opening Settings refreshes FineTune, which is what formerly
  # reinstalled its full-colour submit wrapper after the editor had been touched.
  adb shell input tap 1010 145
  sleep 0.18
  adb shell input tap 1000 245
  sleep 0.14

  adb shell input tap 165 1810
  sleep 0.08
  adb shell input tap 540 1810
  sleep 0.08
  adb shell input tap 880 1810
  sleep 0.10

  # Every eight cycles, really leave the app and resume the same activity/WebView.
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

# Count actual modal state transitions, not assumed coordinates or proxy labels. Emulator
# scheduling occasionally drops a raw coordinate tap, so require a substantial real sample
# instead of pretending all 48 scripted cycles must be recognized.
editor_opens=$(grep -c "EDT_EDITOR_OPEN" smoke/interaction-real-session-log.txt || true)
settings_opens=$(grep -c "EDT_SETTINGS_OPEN" smoke/interaction-real-session-log.txt || true)
nav_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-real-session-log.txt || true)
echo "real_editor_opens=${editor_opens}" | tee -a smoke/interaction-latency.txt
echo "real_settings_opens=${settings_opens}" | tee -a smoke/interaction-latency.txt
echo "real_navigation_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
test "$editor_opens" -ge 30
test "$settings_opens" -ge 30
test "$nav_inputs" -ge 90

# The long-session bug was repeated wrapper installation. Because the diagnostics start
# only after production startup, any repeated submit assignment or recurring render
# transition during the soak is a regression. A couple of one-time settling transitions
# are tolerated, but a growing chain is not.
runtime_snapshots=$(grep -c "EDT_RUNTIME_644" smoke/interaction-real-session-log.txt || true)
render_transitions=$(grep -c "EDT_RUNTIME_RENDER_TRANSITION" smoke/interaction-real-session-log.txt || true)
submit_assignments=$(grep -c "EDT_SUBMIT_ASSIGN" smoke/interaction-real-session-log.txt || true)
course_form_rewraps=$(grep -c "EDT_FAST_FORM_WRAP|form=courseForm" smoke/interaction-real-session-log.txt || true)
echo "runtime_snapshots=${runtime_snapshots}" | tee -a smoke/interaction-latency.txt
echo "render_transition_events_during_soak=${render_transitions}" | tee -a smoke/interaction-latency.txt
echo "submit_assignments_during_soak=${submit_assignments}" | tee -a smoke/interaction-latency.txt
echo "course_form_fast_rewraps_during_soak=${course_form_rewraps}" | tee -a smoke/interaction-latency.txt
test "$runtime_snapshots" -ge 12
test "$render_transitions" -le 2
test "$submit_assignments" -eq 0
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
sleep 0.20
adb shell input tap 880 1810
sleep 0.20
tap_text "Ajouter un cours"
sleep 0.5
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
