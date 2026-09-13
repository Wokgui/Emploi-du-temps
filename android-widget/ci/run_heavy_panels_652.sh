#!/usr/bin/env bash
set -euo pipefail

# 6.52 keeps the complete 6.48 heavy-panel suite and all thresholds/cycle counts.
# Only the two Settings physical taps use the suite's fixed 1080x1920 coordinates.
# The production EDT_HEAVY_INPUT/CHECKPOINT markers still prove that the intended Settings
# open/close actions were actually accepted before any benchmark continues. This removes
# UIAutomator accessibility-tree/compositor hit-test startup flakiness from the physical
# latency measurement without changing what the application executes or what the suite validates.
python3 - <<'PY'
from pathlib import Path
p=Path('android-widget/ci/run_heavy_panels_648.sh')
s=p.read_text()
old="""adb logcat -c
tap_text 'Réglages'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=open|n=1' 200
tap_text '×'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=close|n=1' 200
"""
new="""adb logcat -c
# The viewport is explicitly fixed to 1080x1920 above. Use the real physical hit targets
# directly. On a loaded emulator the compositor can expose the newly opened sheet one frame
# before Android input hit-testing catches up, so retry only while the application has not
# acknowledged the physical input at all. Once EDT_HEAVY_INPUT is seen, never tap again: wait
# for the normal production checkpoint so the measured latency and all thresholds stay intact.
tap_until_heavy_ack() {
  local x=\"$1\" y=\"$2\" input_needle=\"$3\" checkpoint_needle=\"$4\"
  local attempt poll
  for attempt in 1 2 3 4 5; do
    adb shell input tap \"$x\" \"$y\"
    for poll in 1 2 3 4 5 6 7 8; do
      if adb logcat -d -s EDT_HEAVY:I '*:S' | grep -F \"$input_needle\" >/dev/null; then
        wait_for_log \"$checkpoint_needle\" 200
        return 0
      fi
      sleep 0.05
    done
  done
  echo \"Timed out waiting for physical heavy-panel input: $input_needle\" >&2
  return 1
}
tap_until_heavy_ack 1010 145 \\
  'EDT_HEAVY_INPUT|scenario=physical|panel=settings|action=open|n=1' \\
  'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=open|n=1'
# In the native 1080x1920 screenshot the × hit target is centered at (1003,244).
# The earlier 862,210 value came from a UI-scaled preview, not device coordinates.
tap_until_heavy_ack 1003 244 \\
  'EDT_HEAVY_INPUT|scenario=physical|panel=settings|action=close|n=1' \\
  'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=close|n=1'
"""
if old not in s:
    raise SystemExit('6.52 heavy-panel physical Settings anchor not found')
s=s.replace(old,new,1)
Path('/tmp/run_heavy_panels_652_generated.sh').write_text(s)
PY
chmod +x /tmp/run_heavy_panels_652_generated.sh
exec bash /tmp/run_heavy_panels_652_generated.sh
