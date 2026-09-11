#!/usr/bin/env bash
set -euo pipefail

mkdir -p smoke-heavy
PKG=com.wokgui.schedulewidget
ACT=com.wokgui.schedulewidget/.MainActivity
CONTROLLER=android-widget/app/src/main/java/com/wokgui/schedulewidget/HeavyPanelUi648.java

if grep -Eq 'setTimeout|innerHTML|new MutationObserver' "$CONTROLLER"; then
  echo 'Heavy-panel controller must not defer work, rebuild HTML, or register observers' >&2
  exit 1
fi
if [ "$(grep -o 'addEventListener' "$CONTROLLER" | wc -l)" -ne 1 ]; then
  echo 'Heavy-panel input must use one stable listener registration site' >&2
  exit 1
fi

capture_failure() {
  local status=$?
  trap - EXIT
  if [ "$status" -ne 0 ]; then
    adb logcat -d > smoke-heavy/failure.log 2>&1 || true
    adb exec-out screencap -p > smoke-heavy/failure.png 2>/dev/null || true
  fi
  exit "$status"
}
trap capture_failure EXIT

adb install -r smoke-apk/emploi-du-temps-widget.apk
adb shell wm size 1080x1920
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
adb logcat -G 16M || true

assert_clean_log() {
  local file="$1"
  if grep -E 'AndroidRuntime.*Process: com.wokgui.schedulewidget|ANR in com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|TypeError|RangeError|NotFoundError)|EDT_HEAVY_(BENCHMARK.*status=error|PREP_ERROR|OPEN_ERROR)' "$file"; then
    echo 'Crash, ANR or JavaScript error during heavy-panel stress' >&2
    return 1
  fi
}

wait_for_log() {
  local needle="$1"
  local attempts="${2:-1200}"
  for _ in $(seq 1 "$attempts"); do
    if adb logcat -d -s EDT_HEAVY:I '*:S' | grep -F "$needle" >/dev/null; then return 0; fi
    sleep 0.10
  done
  echo "Timed out waiting for log: $needle" >&2
  return 1
}

tap_text() {
  local needle="$1"
  for _ in 1 2 3 4 5; do
    adb shell uiautomator dump /sdcard/edt-heavy.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-heavy.xml /tmp/edt-heavy.xml >/dev/null 2>&1 || true
    local coords
    coords=$(python3 - "$needle" <<'PY'
import re,sys,xml.etree.ElementTree as ET
needle=sys.argv[1].replace('\u00a0',' ').casefold()
try: root=ET.parse('/tmp/edt-heavy.xml').getroot()
except Exception: raise SystemExit(0)
for node in root.iter('node'):
    text=((node.attrib.get('text') or '')+' '+(node.attrib.get('content-desc') or '')).replace('\u00a0',' ').casefold()
    if needle not in text: continue
    m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]',node.attrib.get('bounds',''))
    if not m: continue
    x1,y1,x2,y2=map(int,m.groups())
    if x2>x1 and y2>y1 and y2>0 and y1<1920:
        print((x1+x2)//2,(y1+y2)//2);break
PY
)
    if [ -n "$coords" ]; then adb shell input tap $coords; return 0; fi
    sleep 0.10
  done
  echo "Could not locate visible control: $needle" >&2
  return 1
}

# Two real Android taps establish the input-to-usable-frame measurement independently of the
# deterministic 300-cycle runner used below.
adb shell am force-stop "$PKG" || true
adb logcat -c
adb shell am start -W -n "$ACT" --es open_mode edit >/dev/null
wait_for_log 'EDT_HEAVY_METRICS|ready|' 600
sleep 0.5
adb logcat -d > smoke-heavy/startup.log
if [ "${EDT_HEAVY_REQUIRE_FAST:-0}" = 1 ]; then
  grep -Fq 'EDT_HEAVY_OWNER|ready|' smoke-heavy/startup.log
  grep -Fq 'EDT_HEAVY_METRICS|ready|' smoke-heavy/startup.log
  grep -Fq 'bridgeWrapped=1' smoke-heavy/startup.log
fi
adb logcat -c
tap_text 'Réglages'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=open|n=1' 200
tap_text '×'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=settings|action=close|n=1' 200
tap_text '3G1 ALL'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=course|action=open|n=1' 200
tap_text 'Annuler'
wait_for_log 'EDT_HEAVY_CHECKPOINT|scenario=physical|panel=course|action=close|n=1' 200
adb logcat -d > smoke-heavy/physical-input.log
assert_clean_log smoke-heavy/physical-input.log
grep 'EDT_HEAVY_CHECKPOINT' smoke-heavy/physical-input.log | tee smoke-heavy/physical-input-measurements.txt

run_scenario() {
  local scenario="$1"
  adb shell am force-stop "$PKG" || true
  adb logcat -c
  adb shell am start -W -n "$ACT" --es open_mode edit --es heavy_panel_benchmark "$scenario" --ei heavy_panel_cycles 300 >/dev/null
  wait_for_log "EDT_HEAVY_BENCHMARK|scenario=${scenario}|status=ready|cycles=300" 800
  adb shell dumpsys meminfo "$PKG" > "smoke-heavy/meminfo-${scenario}-before.txt" || true
  wait_for_log "EDT_HEAVY_BENCHMARK|scenario=${scenario}|status=complete|cycles=300" 7200
  adb shell dumpsys meminfo "$PKG" > "smoke-heavy/meminfo-${scenario}-after.txt" || true
  adb logcat -d > "smoke-heavy/${scenario}.log"
  assert_clean_log "smoke-heavy/${scenario}.log"
  grep 'EDT_HEAVY_CHECKPOINT' "smoke-heavy/${scenario}.log" > "smoke-heavy/${scenario}-checkpoints.txt"
  grep 'EDT_HEAVY_SUMMARY' "smoke-heavy/${scenario}.log" > "smoke-heavy/${scenario}-summary.txt"
  grep -Fq "|n=1|" "smoke-heavy/${scenario}-checkpoints.txt"
  grep -Fq "|n=20|" "smoke-heavy/${scenario}-checkpoints.txt"
  grep -Fq "|n=100|" "smoke-heavy/${scenario}-checkpoints.txt"
  if [ "$scenario" != mixed ]; then grep -Fq "|n=300|" "smoke-heavy/${scenario}-checkpoints.txt"; fi
  local before_pss after_pss growth
  before_pss=$(awk '/TOTAL PSS:/{print $3;exit}' "smoke-heavy/meminfo-${scenario}-before.txt" || true)
  after_pss=$(awk '/TOTAL PSS:/{print $3;exit}' "smoke-heavy/meminfo-${scenario}-after.txt" || true)
  if [ -n "$before_pss" ] && [ -n "$after_pss" ]; then
    growth=$((after_pss-before_pss))
    echo "heavy_${scenario}_pss_growth_kb=${growth}" | tee -a smoke-heavy/results.txt
    test "$growth" -le 16384
  fi
  cat "smoke-heavy/${scenario}-summary.txt" | tee -a smoke-heavy/results.txt
}

run_scenario settings
run_scenario course
run_scenario mixed

python3 - "${EDT_HEAVY_REQUIRE_FAST:-0}" <<'PY'
from pathlib import Path
import re,sys

require_fast=sys.argv[1]=='1'
lines=[]
for path in Path('smoke-heavy').glob('*-summary.txt'):
    lines.extend(path.read_text(errors='ignore').splitlines())
if len(lines)<4:
    raise SystemExit(f'expected at least four heavy-panel summaries, got {len(lines)}')

def fields(line):
    return {k:v for k,v in re.findall(r'([A-Za-z][A-Za-z0-9]*)=([^|\s]+)',line)}

for line in lines:
    data=fields(line)
    scenario=data.get('scenario','?'); panel=data.get('panel','?')
    opens=int(data.get('openN',0)); closes=int(data.get('closeN',0))
    expected=300 if scenario in ('settings','course') else 150
    if opens!=expected or closes!=expected:
        raise SystemExit(f'{scenario}/{panel}: expected {expected} complete cycles, got {opens}/{closes}')
    for key in ('listenerDelta','observerDelta','resizeObserverDelta','nodeDelta','errorDelta','mainMutations','renders','bridgeCalls','storageReads','storageWrites','scenarioBridgeCalls','scenarioStorageReads','scenarioStorageWrites','scenarioMainMutations','scenarioRenders'):
        if int(float(data.get(key,-1)))!=0:
            raise SystemExit(f'{scenario}/{panel}: {key} accumulated: {data.get(key)}')
    p50=float(data['openReadyP50']); p95=float(data['openReadyP95']); maximum=float(data['openReadyMax'])
    head=float(data.get('openHeadP50',p50)); tail=float(data.get('openTailP50',p50))
    if p95 > max(240, p50*2.5+80) or maximum>900:
        raise SystemExit(f'{scenario}/{panel}: progressive or extreme opening slowdown: p50={p50}, p95={p95}, max={maximum}')
    if tail > max(head+35,head*1.5):
        raise SystemExit(f'{scenario}/{panel}: progressive slowdown: first-20 p50={head}, last-20 p50={tail}')
    close50=float(data['closeReadyP50']); close95=float(data['closeReadyP95']); closemax=float(data['closeReadyMax'])
    if require_fast and (p50>80 or p95>140 or maximum>350 or close50>80 or close95>140 or closemax>350):
        raise SystemExit(f'{scenario}/{panel}: opening is not yet visually instant: p50={p50}, p95={p95}, max={maximum}')

physical=Path('smoke-heavy/physical-input-measurements.txt').read_text(errors='ignore').splitlines()
if len(physical)!=4:
    raise SystemExit(f'expected four physical input measurements, got {len(physical)}')
if require_fast:
    for line in physical:
        data=fields(line); ready=float(data.get('readyMs',9999))
        if ready>350:
            raise SystemExit(f'physical interaction is not visually instant: {line}')
print(f'heavy_panel_fast_thresholds_enabled={int(require_fast)}')
PY

adb exec-out screencap -p > smoke-heavy/after-300-cycle-suites.png
