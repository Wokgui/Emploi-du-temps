#!/usr/bin/env bash
set -euo pipefail

# 6.52 keeps the complete 6.48 heavy-panel suite and all thresholds/cycle counts.
# Only the two Settings physical taps use the suite's fixed 1080x1920 coordinates.
# The production EDT_HEAVY_CHECKPOINT markers still prove that the intended Settings
# open/close actions were actually accepted before any benchmark continues. This removes
# UIAutomator accessibility-tree startup flakiness from the physical latency measurement
# without changing what the application executes or what the suite validates.
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
# directly and require the production checkpoint after each tap, rather than waiting for
# Android's accessibility snapshot to expose a WebView control that is already usable.
adb shell input tap 1010 145
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=open|n=1' 200
adb shell input tap 862 210
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=close|n=1' 200
"""
if old not in s:
    raise SystemExit('6.52 heavy-panel physical Settings anchor not found')
s=s.replace(old,new,1)
Path('/tmp/run_heavy_panels_652_generated.sh').write_text(s)
PY
chmod +x /tmp/run_heavy_panels_652_generated.sh
exec bash /tmp/run_heavy_panels_652_generated.sh
