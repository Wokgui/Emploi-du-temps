#!/usr/bin/env bash
set -euo pipefail

# Keep every 6.45 regression, but 6.46 emits EDT_NAV_STATS once every 50 navigations.
# The inherited 6.45 test expected >=12 samples from 450 taps; with the 6.46 sampler the
# mathematically correct count is 9. Adapt only that harness threshold, not app behavior.
sed 's/test "$nav_stats" -ge 12/test "$nav_stats" -ge 9/' android-widget/ci/run_smoke_645.sh > /tmp/run_smoke_645_for_646.sh
chmod +x /tmp/run_smoke_645_for_646.sh
bash /tmp/run_smoke_645_for_646.sh

mkdir -p smoke

assert_alive() {
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
}

# Resolve a WebView control from the current accessibility tree.
coord_for() {
  local needle="$1"
  adb shell uiautomator dump /sdcard/edt-646.xml >/dev/null 2>&1 || true
  adb pull /sdcard/edt-646.xml smoke/edt-646.xml >/dev/null 2>&1 || true
  python3 - "$needle" <<'PY'
import re,sys,xml.etree.ElementTree as ET
needle=sys.argv[1].casefold()
root=ET.parse('smoke/edt-646.xml').getroot()
for node in root.iter('node'):
    hay=((node.attrib.get('text') or '')+' '+(node.attrib.get('content-desc') or '')).casefold()
    if needle not in hay: continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.attrib.get('bounds',''))
    if not m: continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2>x1 and y2>y1:
        print((x1+x2)//2,(y1+y2)//2)
        break
PY
}

tap_xy() {
  set -- $1
  adb shell input tap "$1" "$2"
}

require_coord() {
  local needle="$1"
  local out
  out="$(coord_for "$needle")"
  if [ -z "$out" ]; then
    echo "6.46 harness could not resolve control: $needle" >&2
    adb exec-out screencap -p > "smoke/646-missing-${needle// /-}.png" || true
    return 1
  fi
  printf '%s\n' "$out"
}

# The fixed bottom navigation covers roughly the last 130 px of the WebView. UIAutomator can
# still report a DOM node behind it, so only treat a control as physically tappable above y=1700.
scroll_until_tappable() {
  local needle="$1"
  local coord y
  for _ in $(seq 1 5); do
    coord="$(coord_for "$needle")"
    if [ -n "$coord" ]; then
      set -- $coord
      y="$2"
      if [ "$y" -lt 1700 ] && [ "$y" -gt 170 ]; then
        printf '%s\n' "$coord"
        return 0
      fi
    fi
    adb shell input swipe 540 1650 540 900 220
    sleep 0.12
  done
  echo "6.46 harness could not expose control above fixed navigation: $needle" >&2
  adb exec-out screencap -p > "smoke/646-obscured-${needle// /-}.png" || true
  return 1
}

scroll_edit_to_top() {
  for _ in $(seq 1 3); do
    adb shell input swipe 540 900 540 1650 180
    sleep 0.08
  done
}

resolve_top_controls() {
  week_a="$(require_coord 'Semaine A')"
  week_b="$(require_coord 'Semaine B')"
  day_lun="$(require_coord 'Lun')"
  day_jeu="$(require_coord 'Jeu')"
  current_week="$(require_coord 'Cette semaine')"
}

exercise_add_cancel() {
  local add cancel
  adb shell input tap 880 1810
  sleep 0.05
  add="$(scroll_until_tappable 'Ajouter un cours')"
  tap_xy "$add"
  sleep 0.12
  cancel="$(require_coord 'Annuler')"
  tap_xy "$cancel"
  sleep 0.10
  scroll_edit_to_top
  sleep 0.10
  # Rendering and scrolling can recreate/move the edit controls; never reuse stale coordinates.
  resolve_top_controls
}

# Return to Edit in the same activity/WebView left alive by the 6.45 stress.
adb shell input tap 880 1810
sleep 0.4
assert_alive
scroll_edit_to_top
resolve_top_controls

# Physical preflight for the modal path that run #604 exposed as being hidden by bottom nav.
echo "all_controls_phase=add_cancel_preflight" | tee -a smoke/interaction-latency.txt
exercise_add_cancel
assert_alive

adb logcat -c
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-all-controls.txt || true

echo "all_controls_phase=stress_start" | tee -a smoke/interaction-latency.txt
# Repeatedly exercise the major persistent and recreated control families. The purpose is
# not just functional coverage: it proves dynamic DOM replacement cannot accumulate handlers.
for i in $(seq 1 100); do
  # Week selectors rebuild context but must not accumulate listeners.
  tap_xy "$week_a"; sleep 0.035
  tap_xy "$week_b"; sleep 0.035

  # Day buttons are recreated by renderEdit; the delegated router must keep exactly one listener.
  tap_xy "$day_lun"; sleep 0.035
  tap_xy "$day_jeu"; sleep 0.035

  # Bottom navigation remains display-only when data is clean.
  adb shell input tap 165 1810; sleep 0.025
  adb shell input tap 540 1810; sleep 0.025
  adb shell input tap 880 1810; sleep 0.035

  # Settings open/close repeatedly uses persistent top-level controls.
  if [ $((i % 2)) -eq 0 ]; then
    adb shell input tap 1010 145; sleep 0.08
    adb shell input tap 862 210; sleep 0.08
  fi

  # This action really changes persisted state and is allowed to invalidate/re-render views.
  if [ $((i % 10)) -eq 0 ]; then
    tap_xy "$current_week"; sleep 0.12
    resolve_top_controls
  fi

  # Modal controls are rebuilt and may be below the viewport. Scroll to the real physical
  # button, exercise Add + Cancel, restore the top, then refresh every moved coordinate.
  if [ $((i % 5)) -eq 0 ]; then
    exercise_add_cancel
  fi
done
sleep 4

echo "all_controls_phase=stress_complete" | tee -a smoke/interaction-latency.txt
assert_alive
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-after-all-controls.txt || true
adb logcat -d > smoke/all-controls-stress-log.txt || true
adb exec-out screencap -p > smoke/21-after-all-controls-stress.png || true

if grep -E "AndroidRuntime.*Process: com.wokgui.schedulewidget|ANR in com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|NotFoundError)" smoke/all-controls-stress-log.txt; then
  echo "App crash, ANR or JavaScript failure during all-controls stress" >&2
  exit 1
fi

fast_inputs=$(grep -c "EDT_FAST_INPUT|" smoke/all-controls-stress-log.txt || true)
nav_inputs=$(grep -c "EDT_NAV_INPUT|" smoke/all-controls-stress-log.txt || true)
fast_stats=$(grep -c "EDT_FAST_STATS|" smoke/all-controls-stress-log.txt || true)
echo "all_controls_fast_inputs=${fast_inputs}" | tee -a smoke/interaction-latency.txt
echo "all_controls_nav_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
echo "all_controls_fast_stats=${fast_stats}" | tee -a smoke/interaction-latency.txt
test "$fast_inputs" -ge 350
test "$nav_inputs" -ge 250
test "$fast_stats" -ge 3

# Major button families must all have been handled by the one delegated router.
for needle in \
  "EDT_FAST_INPUT|settings|visual|delegated" \
  "EDT_FAST_INPUT|current-week|visual|delegated" \
  "EDT_FAST_INPUT|week-" \
  "EDT_FAST_INPUT|day-" \
  "EDT_FAST_INPUT|add-course|visual|delegated" \
  "EDT_FAST_INPUT|cancel-edit|visual|delegated"; do
  grep -Fq "$needle" smoke/all-controls-stress-log.txt
 done

# The architectural invariant: one event router for the whole document, zero per-control wrappers.
if grep "EDT_FAST_STATS|" smoke/all-controls-stress-log.txt | grep -Ev "routers=1\|wrappers=0"; then
  echo "Delegated interaction invariant changed" >&2
  exit 1
fi

# Compare control latency at the beginning and the end of the stress.
python3 - <<'PY'
from pathlib import Path
import re,statistics
text=Path('smoke/all-controls-stress-log.txt').read_text(errors='ignore')
vals=[int(x) for x in re.findall(r'EDT_FAST_SETTLE\|[^\n]*\|ms=(\d+)',text)]
if len(vals)<10:
    raise SystemExit('not enough delegated control latency samples')
first=vals[:5]; last=vals[-5:]
fmed=statistics.median(first); lmed=statistics.median(last)
with Path('smoke/interaction-latency.txt').open('a') as f:
    f.write(f'all_controls_first_median_ms={fmed}\n')
    f.write(f'all_controls_last_median_ms={lmed}\n')
    f.write(f'all_controls_last_max_ms={max(last)}\n')
print('all_controls_first_median_ms=',fmed)
print('all_controls_last_median_ms=',lmed)
print('all_controls_last_max_ms=',max(last))
if max(last)>700:
    raise SystemExit('a control became visibly slow near the end')
if lmed > max(350, fmed*3+120):
    raise SystemExit('controls progressively slowed during stress')
PY

before_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-before-all-controls.txt || true)
after_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-after-all-controls.txt || true)
if [ -n "$before_pss" ] && [ -n "$after_pss" ]; then
  growth=$((after_pss-before_pss))
  echo "all_controls_pss_growth_kb=${growth}" | tee -a smoke/interaction-latency.txt
  test "$growth" -le 16384
fi
