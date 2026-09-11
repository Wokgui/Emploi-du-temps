#!/usr/bin/env bash
set -euo pipefail
mkdir -p smoke

PKG=com.wokgui.schedulewidget
ACT=com.wokgui.schedulewidget/.MainActivity

adb shell am force-stop "$PKG" || true
adb logcat -c
adb shell am start -W -n "$ACT" --es open_mode edit >/dev/null

ready=0
for _ in $(seq 1 80); do
  sleep 0.15
  if adb logcat -d | grep -Fq 'EDT_INSTANT_STAGE|ready|'; then
    ready=1
    break
  fi
done
if [ "$ready" -ne 1 ]; then
  adb logcat -d > smoke/latency-647-startup-log.txt || true
  adb exec-out screencap -p > smoke/latency-647-startup-failure.png || true
  echo '6.47 mounted-view stage did not initialize' >&2
  exit 1
fi

stage_line=$(adb logcat -d | grep 'EDT_INSTANT_STAGE|ready|' | tail -1)
printf '%s\n' "$stage_line" > smoke/latency-647-stage.txt
python3 - "$stage_line" <<'PY'
import re,sys
line=sys.argv[1]
vals={k:int(v) for k,v in re.findall(r'(today|week|edit)=(\d+)',line)}
if set(vals)!={'today','week','edit'} or min(vals.values())<=0:
    raise SystemExit('one or more mounted views have no measurable height')
print('latency_647_stage_heights='+','.join(f'{k}:{vals[k]}' for k in ('today','week','edit')))
PY

# Dedicated fast physical switching after a clean relaunch. The full 6.46 suite already
# validates screenshots, controls and long sessions; this phase specifically protects the
# 6.47 mounted-view path from regressing into expensive display:none relayouts.
adb logcat -c
adb shell dumpsys meminfo "$PKG" > smoke/meminfo-before-latency-647.txt || true
for i in $(seq 1 90); do
  adb shell input tap 165 1810; sleep 0.035
  adb shell input tap 540 1810; sleep 0.035
  adb shell input tap 880 1810; sleep 0.035
done
sleep 2
adb shell dumpsys meminfo "$PKG" > smoke/meminfo-after-latency-647.txt || true
adb logcat -d > smoke/latency-647-switch-log.txt || true

if grep -E 'AndroidRuntime.*Process: com.wokgui.schedulewidget|ANR in com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|NotFoundError)' smoke/latency-647-switch-log.txt; then
  echo 'App crash, ANR or JavaScript failure during 6.47 latency stress' >&2
  exit 1
fi

nav_inputs=$(grep -c 'EDT_NAV_INPUT|' smoke/latency-647-switch-log.txt || true)
nav_stats=$(grep -c 'EDT_NAV_STATS|' smoke/latency-647-switch-log.txt || true)
nav_settles=$(grep -c 'EDT_NAV_SETTLE|' smoke/latency-647-switch-log.txt || true)
echo "latency_647_navigation_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
echo "latency_647_stats_samples=${nav_stats}" | tee -a smoke/interaction-latency.txt
echo "latency_647_settle_samples=${nav_settles}" | tee -a smoke/interaction-latency.txt
test "$nav_inputs" -ge 250
test "$nav_stats" -ge 5
test "$nav_settles" -ge 4

last_stats=$(grep 'EDT_NAV_STATS|' smoke/latency-647-switch-log.txt | tail -1)
hits=$(printf '%s\n' "$last_stats" | sed -n 's/.*|hits=\([0-9][0-9]*\).*/\1/p')
rt=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderToday=\([0-9][0-9]*\).*/\1/p')
rw=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderWeek=\([0-9][0-9]*\).*/\1/p')
re=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderEdit=\([0-9][0-9]*\).*/\1/p')
test -n "$hits" && test "$hits" -ge 180
test -n "$rt" && test -n "$rw" && test -n "$re"
test $((rt+rw+re)) -le 12

python3 - <<'PY'
from pathlib import Path
import re,statistics
text=Path('smoke/latency-647-switch-log.txt').read_text(errors='ignore')
vals=[int(x) for x in re.findall(r'EDT_NAV_SETTLE\|[^\n]*\|ms=(\d+)',text)]
if len(vals)<4:
    raise SystemExit('not enough 6.47 settled latency samples')
last=vals[-4:]
med=statistics.median(last)
mx=max(last)
with Path('smoke/interaction-latency.txt').open('a') as f:
    f.write(f'latency_647_last_median_ms={med}\nlatency_647_last_max_ms={mx}\n')
print(f'latency_647_last_median_ms={med}')
print(f'latency_647_last_max_ms={mx}')
if mx>180 or med>120:
    raise SystemExit('6.47 mounted-view switching is still too slow')
PY

before_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-before-latency-647.txt || true)
after_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-after-latency-647.txt || true)
if [ -n "$before_pss" ] && [ -n "$after_pss" ]; then
  growth=$((after_pss-before_pss))
  echo "latency_647_pss_growth_kb=${growth}" | tee -a smoke/interaction-latency.txt
  test "$growth" -le 16384
fi

adb exec-out screencap -p > smoke/latency-647-after-switches.png
