#!/usr/bin/env bash
set -euo pipefail
mkdir -p smoke-browser/classes smoke-browser/chunks
src=android-widget/app/src/main/java/com/wokgui/schedulewidget
mapfile -t sources < <(find "$src" -name '*Ui*.java' ! -name UiSettingsStore.java)
javac -encoding UTF-8 -d smoke-browser/classes "${sources[@]}" "$src/RuntimeRepair644.java" android-widget/ci/browser/*.java
java -cp smoke-browser/classes com.wokgui.schedulewidget.ExportUi smoke-browser/chunks
EDT_UI_CHUNKS=smoke-browser/chunks node android-widget/ci/browser/verify.cjs
EDT_UI_CHUNKS=smoke-browser/chunks node android-widget/ci/browser/verify_pipeline_650.cjs
EDT_UI_CHUNKS=smoke-browser/chunks node android-widget/ci/browser/verify_bursts_650.cjs
