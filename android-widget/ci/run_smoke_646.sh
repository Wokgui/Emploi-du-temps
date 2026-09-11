#!/usr/bin/env bash
set -euo pipefail

# Align inherited 6.44/6.45 physical taps with the current 6.46 layout before running them.
# This changes only the CI working copy, never app behavior.
sed -i \
  -e 's/adb shell input tap 862 210/adb shell input tap 1000 245/g' \
  -e 's/adb shell input tap 465 1810/adb shell input tap 540 1810/g' \
  -e 's/adb shell input tap 760 1810/adb shell input tap 880 1810/g' \
  android-widget/ci/run_smoke.sh

# Keep every 6.45 regression. 6.46 emits EDT_NAV_STATS once every 50 navigations, so
# 450 switches produce exactly nine samples rather than the inherited twelve.
sed 's/test "$nav_stats" -ge 12/test "$nav_stats" -ge 9/' android-widget/ci/run_smoke_645.sh > /tmp/run_smoke_645_for_646.sh
chmod +x /tmp/run_smoke_645_for_646.sh
bash /tmp/run_smoke_645_for_646.sh

mkdir -p smoke

assert_alive() {
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
}

tap_coord() {
  local coords="$1"
  set -- $coords
  adb shell input tap "$1" "$2"
}

restore_edit_top() {
  adb shell input tap 880 1810
  sleep 0.28
  for _ in 1 2 3; do
    adb shell input swipe 540 900 540 1650 180
    sleep 0.07
  done
  assert_alive
}

# Resolve all controls from one live accessibility snapshot. Repeating this once per short
# stress block avoids the run-612 failure mode where coordinates captured before 120 mixed
# cycles became stale after later Edit renders and started hitting course cells / Cancel.
resolve_live_controls() {
  local output=""
  for attempt in 1 2 3 4 5; do
    adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-646-live.xml /tmp/edt-646-live.xml >/dev/null 2>&1 || true
    output=$(python3 - <<'PY'
import re, sys, xml.etree.ElementTree as ET
try:
    root=ET.parse('/tmp/edt-646-live.xml').getroot()
except Exception:
    raise SystemExit(0)

wanted={
    'week_a': ('Semaine A','exact'),
    'week_b': ('Semaine B','exact'),
    'day_lun': ('Lun','exact'),
    'day_jeu': ('Jeu','exact'),
    'current_week': ('Cette semaine','prefix'),
}
found={}
for node in root.iter('node'):
    text=(node.attrib.get('text') or '').replace('\u00a0',' ').strip()
    desc=(node.attrib.get('content-desc') or '').replace('\u00a0',' ').strip()
    vals=[v for v in (text,desc) if v]
    if not vals:
        continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.attrib.get('bounds',''))
    if not m:
        continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2<=x1 or y2<=y1:
        continue
    for key,(needle,mode) in wanted.items():
        if key in found:
            continue
        n=needle.casefold()
        ok=any((v.casefold()==n if mode=='exact' else v.casefold().startswith(n)) for v in vals)
        if ok:
            found[key]=f'{(x1+x2)//2} {(y1+y2)//2}'
if len(found) != len(wanted):
    raise SystemExit(0)
for key in wanted:
    print(f'{key}="{found[key]}"')
PY
)
    if [ "$(printf '%s\n' "$output" | grep -c '=')" -eq 5 ]; then
      eval "$output"
      return 0
    fi
    sleep 0.15
  done
  adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
  adb pull /sdcard/edt-646-live.xml smoke/all-controls-live-resolution-failure.xml >/dev/null 2>&1 || true
  adb exec-out screencap -p > smoke/all-controls-live-resolution-failure.png || true
  echo "6.46 could not resolve all live controls" >&2
  return 1
}

# Prove that a freshly resolved set really addresses the intended control families before
# starting the measured long phase.
restore_edit_top
resolve_live_controls
adb logcat -c
tap_coord "$week_a"; sleep 0.12
tap_coord "$week_b"; sleep 0.12
tap_coord "$day_lun"; sleep 0.12
tap_coord "$day_jeu"; sleep 0.12
tap_coord "$current_week"; sleep 0.16
adb logcat -d > smoke/all-controls-preflight-log.txt || true
for needle in \
  "EDT_FAST_INPUT|week-" \
  "EDT_FAST_INPUT|day-" \
  "EDT_FAST_INPUT|current-week|visual|delegated"; do
  grep -Fq "$needle" smoke/all-controls-preflight-log.txt
 done
assert_alive
echo "all_controls_phase=preflight_complete" | tee -a smoke/interaction-latency.txt

adb logcat -c
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-all-controls.txt || true
echo "all_controls_phase=stress_start" | tee -a smoke/interaction-latency.txt

# Twelve short blocks keep one process/WebView alive for the full soak while refreshing the
# live positions after every navigation burst. Each block exercises week/day controls,
# Settings, current-week cycling, then 30 bottom-nav switches. Navigation uses the same
# 60 ms cadence that just achieved 450/450 in the dedicated stress instead of the unrealistic
# 25 ms burst that caused event loss in run 612.
for block in $(seq 1 12); do
  restore_edit_top
  resolve_live_controls

  for i in $(seq 1 10); do
    tap_coord "$week_a"; sleep 0.055
    tap_coord "$week_b"; sleep 0.055
    tap_coord "$day_lun"; sleep 0.055
    tap_coord "$day_jeu"; sleep 0.055

    if [ $((i % 2)) -eq 0 ]; then
      adb shell input tap 1010 145; sleep 0.12
      adb shell input tap 1000 245; sleep 0.12
    fi
  done

  tap_coord "$current_week"; sleep 0.15

  for _ in $(seq 1 10); do
    adb shell input tap 165 1810; sleep 0.06
    adb shell input tap 540 1810; sleep 0.06
    adb shell input tap 880 1810; sleep 0.06
  done
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
# non-navigation inputs, five samples remain the strict mathematical floor.
test "$fast_stats" -ge 5

for needle in \
  "EDT_FAST_INPUT|settings|visual|delegated" \
  "EDT_FAST_INPUT|current-week|visual|delegated" \
  "EDT_FAST_INPUT|week-" \
  "EDT_FAST_INPUT|day-"; do
  grep -Fq "$needle" smoke/all-controls-stress-log.txt
 done

# Architectural invariant: exactly one delegated document router and zero per-control wrappers.
if grep "EDT_FAST_STATS|" smoke/all-controls-stress-log.txt | grep -Ev "routers=1\|wrappers=0"; then
  echo "Delegated interaction invariant changed" >&2
  exit 1
fi

python3 - <<'PY'
from pathlib import Path
import re, statistics
text=Path('smoke/all-controls-stress-log.txt').read_text(errors='ignore')
vals=[int(x) for x in re.findall(r'EDT_FAST_SETTLE\|[^\n]*\|ms=(\d+)', text)]
if len(vals)<10:
    raise SystemExit(f'not enough delegated control latency samples: {len(vals)}')
first=vals[:5]
last=vals[-5:]
fmed=statistics.median(first)
lmed=statistics.median(last)
with Path('smoke/interaction-latency.txt').open('a') as f:
    f.write(f'all_controls_first_median_ms={fmed}\n')
    f.write(f'all_controls_last_median_ms={lmed}\n')
    f.write(f'all_controls_last_max_ms={max(last)}\n')
print('all_controls_first_median_ms=', fmed)
print('all_controls_last_median_ms=', lmed)
print('all_controls_last_max_ms=', max(last))
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
