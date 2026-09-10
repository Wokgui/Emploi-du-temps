#!/usr/bin/env bash
set -euo pipefail

# Align the inherited 6.44/6.45 physical taps with the current 6.46 layout before running it.
# This only changes the CI working copy; it does not alter app behavior.
sed -i \
  -e 's/adb shell input tap 862 210/adb shell input tap 1000 245/g' \
  -e 's/adb shell input tap 465 1810/adb shell input tap 540 1810/g' \
  -e 's/adb shell input tap 760 1810/adb shell input tap 880 1810/g' \
  android-widget/ci/run_smoke.sh

# Keep every 6.45 regression, but 6.46 emits EDT_NAV_STATS once every 50 navigations.
# The inherited 6.45 test expected >=12 samples from 450 taps; with the 6.46 sampler the
# mathematically correct count is 9. Adapt only that harness sample count, not app behavior.
sed 's/test "$nav_stats" -ge 12/test "$nav_stats" -ge 9/' android-widget/ci/run_smoke_645.sh > /tmp/run_smoke_645_for_646.sh
chmod +x /tmp/run_smoke_645_for_646.sh
bash /tmp/run_smoke_645_for_646.sh

mkdir -p smoke

assert_alive() {
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
}

# Resolve a control from the accessibility tree captured at the exact current scroll/layout.
# The stable regression suite already uses the same UIAutomator-bounds -> input-tap path for
# real WebView controls. This avoids guessing pixel coordinates from a screenshot taken at a
# different instant. `exact` matches the whole label; `prefix` is used for Cette semaine : X.
resolve_control() {
  local needle="$1"
  local mode="${2:-exact}"
  local slug
  slug=$(printf '%s' "$needle" | tr ' /:' '____')
  for attempt in $(seq 1 5); do
    adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-646-live.xml /tmp/edt-646-live.xml >/dev/null 2>&1 || true
    local coords=""
    coords=$(python3 - "$needle" "$mode" <<'PY'
import re,sys,xml.etree.ElementTree as ET
needle=sys.argv[1].replace('\u00a0',' ').strip().casefold()
mode=sys.argv[2]
try:
    root=ET.parse('/tmp/edt-646-live.xml').getroot()
except Exception:
    raise SystemExit(0)
for node in root.iter('node'):
    text=(node.attrib.get('text') or '').replace('\u00a0',' ').strip()
    desc=(node.attrib.get('content-desc') or '').replace('\u00a0',' ').strip()
    candidates=[text,desc]
    ok=any((v.casefold()==needle if mode=='exact' else v.casefold().startswith(needle)) for v in candidates if v)
    if not ok:
        continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.attrib.get('bounds',''))
    if not m:
        continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2<=x1 or y2<=y1:
        continue
    # Prefer the actionable node itself. Chromium occasionally marks a child non-clickable;
    # its bounds are still valid and this is the same fallback used by the inherited harness.
    print((x1+x2)//2,(y1+y2)//2)
    break
PY
)
    if [ -n "$coords" ]; then
      printf '%s\n' "$coords"
      return 0
    fi
    sleep 0.15
  done
  adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
  adb pull /sdcard/edt-646-live.xml "smoke/missing-${slug}.xml" >/dev/null 2>&1 || true
  adb exec-out screencap -p > "smoke/missing-${slug}.png" || true
  echo "6.46 could not resolve live control: ${needle}" >&2
  return 1
}

tap_coord() {
  local coords="$1"
  set -- $coords
  adb shell input tap "$1" "$2"
}

wait_fast_marker() {
  local marker="$1"
  local before="$2"
  for _ in $(seq 1 25); do
    sleep 0.06
    local after
    after=$(adb logcat -d | grep -Fc "$marker" || true)
    if [ "$after" -gt "$before" ]; then return 0; fi
  done
  return 1
}

# Resolve and prove one live control. On a miss, refresh its bounds once before failing.
prove_control() {
  local needle="$1" mode="$2" marker="$3"
  local coords before
  for attempt in 1 2; do
    coords=$(resolve_control "$needle" "$mode") || return 1
    before=$(adb logcat -d | grep -Fc "$marker" || true)
    tap_coord "$coords"
    if wait_fast_marker "$marker" "$before"; then
      printf '%s\n' "$coords"
      return 0
    fi
  done
  adb logcat -d > smoke/all-controls-live-resolution-failure-log.txt || true
  adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
  adb pull /sdcard/edt-646-live.xml smoke/all-controls-live-resolution-failure.xml >/dev/null 2>&1 || true
  adb exec-out screencap -p > smoke/all-controls-live-resolution-failure.png || true
  echo "6.46 resolved but did not receive marker for: ${needle}" >&2
  return 1
}

# Restore Edit and top before resolving controls. The bottom navigation itself has just been
# validated by 450 physical switches, so there is no redundant navigation preflight here.
adb shell input tap 880 1810
sleep 0.45
for _ in $(seq 1 3); do
  adb shell input swipe 540 900 540 1650 180
  sleep 0.08
done
assert_alive

# Resolve all non-navigation controls from the live hierarchy and verify each one once. The
# successful coordinates are then reused for the high-frequency stress so UIAutomator dump
# latency is not accidentally included in interaction timing measurements.
adb logcat -c
week_a=$(prove_control "Semaine A" exact "EDT_FAST_INPUT|week-")
week_b=$(prove_control "Semaine B" exact "EDT_FAST_INPUT|week-")
day_lun=$(prove_control "Lun" exact "EDT_FAST_INPUT|day-")
day_jeu=$(prove_control "Jeu" exact "EDT_FAST_INPUT|day-")
current_week=$(prove_control "Cette semaine" prefix "EDT_FAST_INPUT|current-week|visual|delegated")
assert_alive
adb logcat -d > smoke/all-controls-preflight-log.txt || true
echo "all_controls_phase=preflight_complete" | tee -a smoke/interaction-latency.txt

adb logcat -c
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-all-controls.txt || true
echo "all_controls_phase=stress_start" | tee -a smoke/interaction-latency.txt

# 120 cycles deliberately exceed the previous 100-cycle plan. This repeatedly exercises
# persistent controls, controls recreated by Edit rendering, and all three navigation targets
# after the already-completed 450-tab soak. Modal/editor controls are covered by the inherited
# real-editor long session before this phase.
for i in $(seq 1 120); do
  tap_coord "$week_a"; sleep 0.035
  tap_coord "$week_b"; sleep 0.035

  tap_coord "$day_lun"; sleep 0.035
  tap_coord "$day_jeu"; sleep 0.035

  adb shell input tap 165 1810; sleep 0.025
  adb shell input tap 540 1810; sleep 0.025
  adb shell input tap 880 1810; sleep 0.035

  if [ $((i % 2)) -eq 0 ]; then
    adb shell input tap 1010 145; sleep 0.10
    adb shell input tap 1000 245; sleep 0.10
  fi

  if [ $((i % 10)) -eq 0 ]; then
    tap_coord "$current_week"; sleep 0.12
  fi
done
sleep 4

echo "all_controls_phase=stress_complete" | tee -a smoke/interaction-latency.txt
assert_alive
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-after-all-controls.txt || true
adb logcat -d > smoke/all-controls-stress-log.txt || true
adb exec-out screencap -p > smoke/22-after-all-controls-stress.png || true

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
test "$fast_inputs" -ge 500
test "$nav_inputs" -ge 330
# FastInteraction emits one invariant sample every 100 delegated clicks. With >=500
# non-navigation inputs in this phase, five samples are the strict mathematical floor.
test "$fast_stats" -ge 5

for needle in \
  "EDT_FAST_INPUT|settings|visual|delegated" \
  "EDT_FAST_INPUT|current-week|visual|delegated" \
  "EDT_FAST_INPUT|week-" \
  "EDT_FAST_INPUT|day-"; do
  grep -Fq "$needle" smoke/all-controls-stress-log.txt
 done

# Architectural invariant: one document router, zero per-control wrappers throughout stress.
if grep "EDT_FAST_STATS|" smoke/all-controls-stress-log.txt | grep -Ev "routers=1\|wrappers=0"; then
  echo "Delegated interaction invariant changed" >&2
  exit 1
fi

python3 - <<'PY'
from pathlib import Path
import re,statistics
text=Path('smoke/all-controls-stress-log.txt').read_text(errors='ignore')
vals=[int(x) for x in re.findall(r'EDT_FAST_SETTLE\|[^\n]*\|ms=(\d+)',text)]
if len(vals)<10:
    raise SystemExit(f'not enough delegated control latency samples: {len(vals)}')
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
