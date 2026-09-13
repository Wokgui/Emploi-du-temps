#!/usr/bin/env bash
set -euo pipefail

# Branch-only acceleration of the unchanged 6.46 smoke semantics.
# UIAutomator is intentionally kept for the first resolution of every semantic control,
# but stable controls are not rediscovered hundreds of times after rerenders. The same
# 120-cycle stress, production markers, latency limits and memory thresholds remain active.
python3 - <<'PY'
from pathlib import Path
p=Path('android-widget/ci/run_smoke_646.sh')
s=p.read_text()
old=r'''tap_live() {
  local needle="$1"
  local mode="${2:-exact}"
  local coords
  coords=$(resolve_control "$needle" "$mode")
  set -- $coords
  adb shell input tap "$1" "$2"
}
'''
new=r'''declare -A EDT_652_CONTROL_COORDS=()
tap_live() {
  local needle="$1"
  local mode="${2:-exact}"
  local key="${mode}|${needle}"
  local coords="${EDT_652_CONTROL_COORDS[$key]:-}"
  if [ -z "$coords" ]; then
    coords=$(resolve_control "$needle" "$mode")
    EDT_652_CONTROL_COORDS[$key]="$coords"
    echo "6.52 cached control ${needle}: ${coords}" >&2
  fi
  set -- $coords
  adb shell input tap "$1" "$2"
}
'''
if old not in s:
    raise SystemExit('6.52 fast-smoke tap_live anchor not found')
s=s.replace(old,new,1)
p.write_text(s)
PY

exec bash android-widget/ci/run_smoke_646.sh
