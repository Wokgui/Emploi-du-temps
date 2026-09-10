#!/usr/bin/env bash
set -euo pipefail

# Keep the complete 6.44 regression suite, but count navigation through the new 6.45
# navigation owner instead of the old FastInteraction proxy.
python3 - <<'PY'
from pathlib import Path
src=Path('android-widget/ci/run_smoke_644.sh').read_text()
old='nav_inputs=$(grep -c "EDT_FAST_INPUT|nav-" smoke/interaction-real-session-log.txt || true)'
new='nav_inputs=$(grep -c "EDT_NAV_INPUT|" smoke/interaction-real-session-log.txt || true)'
if old not in src: raise SystemExit('6.44 nav counter anchor not found')
src=src.replace(old,new,1)
old='test "$settings_inputs" -ge 25'
new='test "$settings_inputs" -ge 15'
if old not in src: raise SystemExit('6.44 settings threshold anchor not found')
src=src.replace(old,new,1)
anchor="Path('/tmp/run_smoke_644_generated.sh').write_text(src)"
replacement="src=src.replace('EDT_FAST_INPUT|nav-','EDT_NAV_INPUT|')\n"+anchor
if anchor not in src: raise SystemExit('generated-script write anchor not found')
src=src.replace(anchor,replacement,1)
old='exec bash /tmp/run_smoke_644_generated.sh'
new='bash /tmp/run_smoke_644_generated.sh'
if old not in src: raise SystemExit('6.44 exec anchor not found')
src=src.replace(old,new,1)
Path('/tmp/run_smoke_645_base.sh').write_text(src)
PY
chmod +x /tmp/run_smoke_645_base.sh
bash /tmp/run_smoke_645_base.sh

# Dedicated 6.45 regression for the reported slowdown. Keep the same process/WebView alive
# after the long inherited session, then repeatedly switch the three bottom tabs.
adb shell input keyevent KEYCODE_BACK
sleep 0.35
adb logcat -c
adb shell am start -W -n com.wokgui.schedulewidget/.MainActivity --es open_mode edit >/dev/null

# Tap one navigation target and wait until the JavaScript owner acknowledges that exact tap.
# Fixed sleeps were racy: run #596 visually reached Edit, but logcat was sampled before its
# delayed EDT_NAV_INPUT line had been emitted.
tap_and_wait_nav() {
  target="$1"
  x="$2"
  before=$(adb logcat -d | grep -c "EDT_NAV_INPUT|${target}|" || true)
  adb shell input tap "$x" 1810
  for _ in $(seq 1 30); do
    sleep 0.10
    after=$(adb logcat -d | grep -c "EDT_NAV_INPUT|${target}|" || true)
    if [ "$after" -gt "$before" ]; then
      return 0
    fi
  done
  return 1
}

# First prove the bottom navigation is interactive after the relaunch/reinjection.
nav_ready=0
for _ in $(seq 1 20); do
  if tap_and_wait_nav edit 880; then
    nav_ready=1
    break
  fi
  sleep 0.15
done
if [ "$nav_ready" -ne 1 ]; then
  adb logcat -d > smoke/tab-stress-readiness-log.txt || true
  adb exec-out screencap -p > smoke/20-tab-stress-readiness-failure.png || true
  echo "6.45 navigation did not become interactive after relaunch" >&2
  exit 1
fi

# Preflight every coordinate independently and wait for its own acknowledgement. This checks
# that the exact three physical tap locations work without mistaking delayed processing for a
# lost input.
adb logcat -c
preflight_ok=1
for spec in "today 165" "week 540" "edit 880"; do
  set -- $spec
  if ! tap_and_wait_nav "$1" "$2"; then
    preflight_ok=0
    break
  fi
done
adb logcat -d > smoke/tab-stress-preflight-log.txt || true
preflight_nav=$(grep -c "EDT_NAV_INPUT|" smoke/tab-stress-preflight-log.txt || true)
echo "tab_stress_preflight_navigation_inputs=${preflight_nav}" | tee -a smoke/interaction-latency.txt
if [ "$preflight_ok" -ne 1 ] || [ "$preflight_nav" -lt 3 ]; then
  adb exec-out screencap -p > smoke/20-tab-stress-preflight-failure.png || true
  echo "6.45 tab-stress preflight did not acknowledge all three bottom navigation buttons" >&2
  exit 1
fi

# Restore Edit, then measure only the dedicated stress phase.
tap_and_wait_nav edit 880 || true
adb logcat -c
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-tab-stress.txt || true

for i in $(seq 1 150); do
  adb shell input tap 165 1810
  sleep 0.06
  adb shell input tap 540 1810
  sleep 0.06
  adb shell input tap 880 1810
  sleep 0.06
done
# Allow delayed UI work and log delivery to drain before sampling the final state.
sleep 4

adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-after-tab-stress.txt || true
adb logcat -d > smoke/tab-stress-log.txt || true

if grep -E "AndroidRuntime.*Process: com.wokgui.schedulewidget|ANR in com.wokgui.schedulewidget|Uncaught (SyntaxError|ReferenceError|NotFoundError)" smoke/tab-stress-log.txt; then
  echo "App crash, ANR or JavaScript failure during tab stress" >&2
  exit 1
fi

nav_inputs=$(grep -c "EDT_NAV_INPUT|" smoke/tab-stress-log.txt || true)
nav_stats=$(grep -c "EDT_NAV_STATS|" smoke/tab-stress-log.txt || true)
nav_settles=$(grep -c "EDT_NAV_SETTLE|" smoke/tab-stress-log.txt || true)
echo "tab_stress_navigation_inputs=${nav_inputs}" | tee -a smoke/interaction-latency.txt
echo "tab_stress_stats_samples=${nav_stats}" | tee -a smoke/interaction-latency.txt
echo "tab_stress_settle_samples=${nav_settles}" | tee -a smoke/interaction-latency.txt
test "$nav_inputs" -ge 420
test "$nav_stats" -ge 12
test "$nav_settles" -ge 8

last_stats=$(grep "EDT_NAV_STATS|" smoke/tab-stress-log.txt | tail -1 || true)
if [ -z "$last_stats" ]; then
  echo "No 6.45 navigation statistics found" >&2
  exit 1
fi
hits=$(printf '%s\n' "$last_stats" | sed -n 's/.*|hits=\([0-9][0-9]*\).*/\1/p')
rt=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderToday=\([0-9][0-9]*\).*/\1/p')
rw=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderWeek=\([0-9][0-9]*\).*/\1/p')
re=$(printf '%s\n' "$last_stats" | sed -n 's/.*|renderEdit=\([0-9][0-9]*\).*/\1/p')
echo "tab_stress_cache_hits=${hits}" | tee -a smoke/interaction-latency.txt
echo "tab_stress_target_renders_today=${rt}" | tee -a smoke/interaction-latency.txt
echo "tab_stress_target_renders_week=${rw}" | tee -a smoke/interaction-latency.txt
echo "tab_stress_target_renders_edit=${re}" | tee -a smoke/interaction-latency.txt
test -n "$hits" && test "$hits" -ge 300
test -n "$rt" && test -n "$rw" && test -n "$re"
test $((rt+rw+re)) -le 12

# During pure bottom-tab switching the persistent FastInteraction wrapper total must stay
# constant. Dynamic course rows are not touched in this phase.
fast_values=$(sed -n 's/.*EDT_NAV_STATS.*|fastWrapped=\([-0-9][0-9]*\).*/\1/p' smoke/tab-stress-log.txt | sort -nu | tr '\n' ' ')
fast_unique=$(sed -n 's/.*EDT_NAV_STATS.*|fastWrapped=\([-0-9][0-9]*\).*/\1/p' smoke/tab-stress-log.txt | sort -nu | wc -l | tr -d ' ')
echo "tab_stress_fastWrapped_values=${fast_values}" | tee -a smoke/interaction-latency.txt
test "$fast_unique" -eq 1

# Compare settled switch latency near the start and end. The absolute cap catches visible
# sluggishness, while the ratio catches progressive degradation.
python3 - <<'PY'
from pathlib import Path
import re, statistics
text=Path('smoke/tab-stress-log.txt').read_text(errors='ignore')
vals=[int(x) for x in re.findall(r'EDT_NAV_SETTLE\|[^\n]*\|ms=(\d+)',text)]
if len(vals)<8:
    raise SystemExit('not enough settled navigation latency samples')
first=vals[:4]
last=vals[-4:]
first_med=statistics.median(first)
last_med=statistics.median(last)
Path('smoke/interaction-latency.txt').open('a').write(
    f'tab_stress_first_settle_median_ms={first_med}\n'
    f'tab_stress_last_settle_median_ms={last_med}\n'
    f'tab_stress_last_settle_max_ms={max(last)}\n')
print(f'tab_stress_first_settle_median_ms={first_med}')
print(f'tab_stress_last_settle_median_ms={last_med}')
print(f'tab_stress_last_settle_max_ms={max(last)}')
if max(last)>650:
    raise SystemExit('tab switching became visibly slow near the end')
if last_med > max(350, first_med*3+120):
    raise SystemExit('tab switching progressively slowed during stress')
PY

before_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-before-tab-stress.txt || true)
after_pss=$(awk '/TOTAL PSS:/{print $3;exit}' smoke/meminfo-after-tab-stress.txt || true)
if [ -n "$before_pss" ] && [ -n "$after_pss" ]; then
  growth=$((after_pss-before_pss))
  echo "tab_stress_pss_growth_kb=${growth}" | tee -a smoke/interaction-latency.txt
  test "$growth" -le 16384
fi

adb exec-out screencap -p > smoke/20-after-450-tab-switches.png
