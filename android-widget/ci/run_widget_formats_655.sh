#!/usr/bin/env bash
set -euo pipefail

PKG="com.wokgui.schedulewidget"
ACTIVITY="$PKG/.ImportReviewPreviewActivity"
OUT_DIR="smoke/widget-formats-655"
mkdir -p "$OUT_DIR"

wait_for_text() {
  local needle="$1"
  local tries=0
  while (( tries < 30 )); do
    adb shell uiautomator dump /sdcard/edt-widget-picker.xml >/dev/null 2>&1 || true
    adb pull /sdcard/edt-widget-picker.xml "$OUT_DIR/window.xml" >/dev/null 2>&1 || true
    if [[ -f "$OUT_DIR/window.xml" ]] && grep -q "$needle" "$OUT_DIR/window.xml"; then
      return 0
    fi
    sleep 0.25
    tries=$((tries + 1))
  done
  echo "Widget picker did not expose expected text: $needle" >&2
  return 1
}

tap_text_prefix() {
  local prefix="$1"
  adb shell uiautomator dump /sdcard/edt-widget-picker.xml >/dev/null 2>&1
  adb pull /sdcard/edt-widget-picker.xml "$OUT_DIR/window.xml" >/dev/null 2>&1
  python3 - "$OUT_DIR/window.xml" "$prefix" <<'PY'
import re, subprocess, sys, xml.etree.ElementTree as ET
path, prefix = sys.argv[1], sys.argv[2]
root = ET.parse(path).getroot()
for node in root.iter('node'):
    text = node.attrib.get('text','')
    if text.startswith(prefix):
        m = re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', node.attrib.get('bounds',''))
        if not m:
            raise SystemExit('No bounds for '+prefix)
        x1,y1,x2,y2 = map(int,m.groups())
        subprocess.check_call(['adb','shell','input','tap',str((x1+x2)//2),str((y1+y2)//2)])
        raise SystemExit(0)
raise SystemExit('Text not found: '+prefix)
PY
}

assert_picker() {
  local id="$1"
  adb shell am force-stop "$PKG" >/dev/null
  adb shell am start -W -n "$ACTIVITY" --ei appWidgetId "$id" >/dev/null
  wait_for_text "Choix du format du widget"
  grep -q "Version actuelle" "$OUT_DIR/window.xml"
  grep -q "Version 3" "$OUT_DIR/window.xml"
  grep -q "Version 4" "$OUT_DIR/window.xml"
  adb exec-out screencap -p > "$OUT_DIR/picker-$id.png"
}

choose_and_assert() {
  local id="$1"
  local prefix="$2"
  local expected="$3"
  assert_picker "$id"
  tap_text_prefix "$prefix"
  sleep 0.2
  tap_text_prefix "Installer ce format"
  sleep 0.4
  adb shell run-as "$PKG" cat shared_prefs/widget_layout_formats_v1.xml > "$OUT_DIR/prefs-$id.xml"
  grep -Eq "name=\"format_${id}\"[^>]*value=\"${expected}\"|value=\"${expected}\"[^>]*name=\"format_${id}\"" "$OUT_DIR/prefs-$id.xml"
}

choose_and_assert 4241 "Version actuelle" 1
choose_and_assert 4243 "Version 3" 3
choose_and_assert 4244 "Version 4" 4

echo "Widget format chooser 6.55 OK: classic=1 condensed=3 mini=4"
