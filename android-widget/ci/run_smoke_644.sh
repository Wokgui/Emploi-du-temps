#!/usr/bin/env bash
set -euo pipefail

# Keep the complete regression suite from run_smoke.sh, but replace the brittle
# accessibility lookup used for the long-session course editor loop. A visible course
# is tapped at a stable position in Edit mode; tapping the dark backdrop invokes the
# app's own modal handler, which in turn calls the real cancelEdit button.
python3 - <<'PY'
from pathlib import Path
src=Path('android-widget/ci/run_smoke.sh').read_text()
old='''adb shell input tap 760 1810
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
'''
new='''adb shell input tap 880 1810
sleep 0.35
# Ensure the Edit page is at the top so the first course stays under this coordinate.
adb shell input swipe 540 560 540 1450 160 || true
sleep 0.2
adb logcat -c
for i in $(seq 1 18); do
  # Open a real existing course editor. This exercises the same modal-open refresh path
  # as normal use without relying on an off-screen accessibility node.
  adb shell input tap 500 900
  sleep 0.18
  # The sheet occupies at most 86% of the screen. Its dark backdrop is therefore safely
  # tappable near the top and the app itself routes that tap to cancelEdit.click().
  adb shell input tap 540 105
  sleep 0.16

  # Real Settings open/close path.
  adb shell input tap 1010 145
  sleep 0.18
  adb shell input tap 862 210
  sleep 0.15

  # Real navigation in between editor/settings use.
  adb shell input tap 165 1810
  sleep 0.08
  adb shell input tap 465 1810
  sleep 0.08
  adb shell input tap 880 1810
  sleep 0.10

  if [ $((i % 3)) -eq 0 ]; then
    assert_main_alive
    sleep 0.22
  fi
done
sleep 1
assert_main_alive
adb logcat -d > smoke/interaction-real-session-log.txt || true
real_editor_inputs=$(grep -c "EDT_FAST_INPUT|control|visual" smoke/interaction-real-session-log.txt || true)
echo "real_editor_control_inputs_seen=${real_editor_inputs}" | tee -a smoke/interaction-latency.txt
test "$real_editor_inputs" -ge 12
assert_clean_log smoke/interaction-real-session-log.txt
measure_settings_latency settings_after_real_editor_settings_session
sleep 0.4
adb exec-out screencap -p > smoke/02c-settings-after-real-session.png

# Validate the requested Add-course layout. Accessibility is reliable for the large
# Add-course button itself; closing uses the native backdrop/cancel path, not the
# off-screen Annuler node that made the previous test brittle.
adb shell input tap 862 210
sleep 0.25
adb shell input tap 880 1810
sleep 0.25
tap_text "Ajouter un cours"
sleep 0.55
adb exec-out screencap -p > smoke/02d-course-editor-layout.png
adb shell input tap 540 105
sleep 0.2
assert_main_alive
'''
if old not in src:
    raise SystemExit('expected real-session block not found')
Path('/tmp/run_smoke_644_generated.sh').write_text(src.replace(old,new))
PY
chmod +x /tmp/run_smoke_644_generated.sh
exec bash /tmp/run_smoke_644_generated.sh
