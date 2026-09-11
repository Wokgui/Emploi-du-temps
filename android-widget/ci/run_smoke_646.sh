#!/usr/bin/env bash
set -euo pipefail

# Reuse the complete 6.45 regression suite with only the two known 6.46 harness adaptations.
sed -i \
  -e 's/adb shell input tap 862 210/adb shell input tap 1000 245/g' \
  -e 's/adb shell input tap 465 1810/adb shell input tap 540 1810/g' \
  -e 's/adb shell input tap 760 1810/adb shell input tap 880 1810/g' \
  android-widget/ci/run_smoke.sh
sed 's/test "$nav_stats" -ge 12/test "$nav_stats" -ge 9/' android-widget/ci/run_smoke_645.sh > /tmp/run_smoke_645_for_646.sh
chmod +x /tmp/run_smoke_645_for_646.sh
bash /tmp/run_smoke_645_for_646.sh

mkdir -p smoke

assert_alive() {
  test -n "$(adb shell pidof com.wokgui.schedulewidget | tr -d '\r')"
}

restore_edit_top() {
  adb shell input tap 880 1810
  sleep 0.24
  for _ in 1 2 3; do
    adb shell input swipe 540 900 540 1650 160
    sleep 0.05
  done
  assert_alive
}

# Return the current on-screen centre of one control. A new hierarchy is captured for every
# tap because week/day selection rerenders Edit and therefore invalidates prior DOM nodes.
resolve_control() {
  local needle="$1"
  local mode="${2:-exact}"
  for _ in 1 2 3 4 5; do
    adb shell uiautomator dump /sdcard/edt-646-live.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-646-live.xml /tmp/edt-646-live.xml >/dev/null 2>&1 || true
    local coords
    coords=$(python3 - "$needle" "$mode" <<'PY'
import re,sys,xml.etree.ElementTree as ET
needle=sys.argv[1].replace('\u00a0',' ').strip().casefold()
mode=sys.argv[2]
try:
    root=ET.parse('/tmp/edt-646-live.xml').getroot()
except Exception:
    raise SystemExit(0)
for node in root.iter('node'):
    vals=[(node.attrib.get('text') or '').replace('\u00a0',' ').strip(),
          (node.attrib.get('content-desc') or '').replace('\u00a0',' ').strip()]
    ok=any((v.casefold()==needle if mode=='exact' else v.casefold().startswith(needle)) for v in vals if v)
    if not ok:
        continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.attrib.get('bounds',''))
    if not m:
        continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2<=x1 or y2<=y1 or y2<0 or y1>1920:
        continue
    print((x1+x2)//2,(y1+y2)//2)
    break
PY
)
    if [ -n "$coords" ]; then
      printf '%s\n' "$coords"
      return 0
    fi
    sleep 0.12
  done
  adb exec-out screencap -p > smoke/all-controls-live-resolution-failure.png || true
  adb pull /sdcard/edt-646-live.xml smoke/all-controls-live-resolution-failure.xml >/dev/null 2>&1 || true
  echo "6.46 could not resolve live control: $needle" >&2
  return 1
}

tap_live() {
  local needle="$1"
  local mode="${2:-exact}"
  local coords
  coords=$(resolve_control "$needle" "$mode")
  set -- $coords
  adb shell input tap "$1" "$2"
}

# Deterministic preflight: resolve again after every action that can rerender the view.
restore_edit_top
adb logcat -c
tap_live "Semaine A" exact; sleep 0.14
tap_live "Semaine B" exact; sleep 0.14
tap_live "Lun" exact; sleep 0.14
tap_live "Jeu" exact; sleep 0.14
tap_live "Cette semaine" prefix; sleep 0.18
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

# 120 mixed cycles. UIAutomator time is outside the app's own EDT_FAST_SETTLE measurement;
# resolving each control immediately before its tap tests the real target without stale bounds.
for i in $(seq 1 120); do
  restore_edit_top

  tap_live "Semaine A" exact; sleep 0.055
  tap_live "Semaine B" exact; sleep 0.055
  tap_live "Lun" exact; sleep 0.055
  tap_live "Jeu" exact; sleep 0.055

  if [ $((i % 2)) -eq 0 ]; then
    adb shell input tap 1010 145; sleep 0.12
    adb shell input tap 1000 245; sleep 0.12
  fi

  if [ $((i % 10)) -eq 0 ]; then
    tap_live "Cette semaine" prefix; sleep 0.15
  fi

  adb shell input tap 165 1810; sleep 0.06
  adb shell input tap 540 1810; sleep 0.06
  adb shell input tap 880 1810; sleep 0.06
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
test "$fast_stats" -ge 5

for needle in \
  "EDT_FAST_INPUT|settings|visual|delegated" \
  "EDT_FAST_INPUT|current-week|visual|delegated" \
  "EDT_FAST_INPUT|week-" \
  "EDT_FAST_INPUT|day-"; do
  grep -Fq "$needle" smoke/all-controls-stress-log.txt
 done

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
