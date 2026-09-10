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

# Resolve a visible WebView control once, then reuse its physical location during the stress.
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

# Return to Edit in the same activity/WebView left alive by the 6.45 stress.
adb shell input tap 880 1810
sleep 0.4
assert_alive

week_a="$(coord_for 'Semaine A')"
week_b="$(coord_for 'Semaine B')"
day_lun="$(coord_for 'Lun')"
day_jeu="$(coord_for 'Jeu')"
current_week="$(coord_for 'Cette semaine')"
add_course="$(coord_for 'Ajouter un cours')"
for v in week_a week_b day_lun day_jeu current_week add_course; do
  test -n "${!v}"
done

# Resolve Cancel once while the add-course sheet is open.
tap_xy "$add_course"
sleep 0.25
cancel_edit="$(coord_for 'Annuler')"
test -n "$cancel_edit"
tap_xy "$cancel_edit"
sleep 0.25

adb logcat -c
adb shell dumpsys meminfo com.wokgui.schedulewidget > smoke/meminfo-before-all-controls.txt || true

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
  fi

  # Modal controls are also rebuilt; exercise Add + Cancel without modifying timetable data.
  if [ $((i % 5)) -eq 0 ]; then
    adb shell input tap 880 1810; sleep 0.05
    tap_xy "$day_jeu"; sleep 0.05
    tap_xy "$add_course"; sleep 0.08
    tap_xy "$cancel_edit"; sleep 0.08
  fi
done
sleep 4

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
