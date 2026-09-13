#!/usr/bin/env bash
set -euo pipefail

# Branch-only acceleration/stabilization of the unchanged 6.46 smoke semantics.
# Keep every cycle and threshold. The application has a dedicated EDT_NAV_INPUT owner now,
# so the inherited quick baseline must count that production marker rather than the removed
# FastInteraction navigation proxy. Also restore the inherited Settings threshold to 25.
python3 - <<'PY'
from pathlib import Path

base=Path('android-widget/ci/run_smoke.sh')
s=base.read_text()
old='stress_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-stress-log.txt || true)'
new='stress_inputs=$(grep -c "EDT_NAV_INPUT|" smoke/interaction-stress-log.txt || true)'
if old not in s:
    raise SystemExit('6.52 quick-navigation counter anchor not found')
s=s.replace(old,new,1)
base.write_text(s)

p645=Path('android-widget/ci/run_smoke_645.sh')
s645=p645.read_text()
old645="new='test \"$settings_inputs\" -ge 15'"
new645="new='test \"$settings_inputs\" -ge 25'"
if old645 not in s645:
    raise SystemExit('6.52 Settings threshold restore anchor not found')
s645=s645.replace(old645,new645,1)
p645.write_text(s645)

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
