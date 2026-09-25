#!/usr/bin/env bash
set -euo pipefail

mkdir -p smoke-browser/classes smoke-browser/chunks
src=android-widget/app/src/main/java/com/wokgui/schedulewidget
mapfile -t sources < <(find "$src" -name '*Ui*.java' ! -name UiSettingsStore.java)
javac -encoding UTF-8 -d smoke-browser/classes "${sources[@]}" "$src/RuntimeRepair644.java" "$src/LegacyChainRepair651.java" "$src/CondensedRowSizing.java" "$src/WidgetAutoLayoutSizing.java" "$src/WidgetHeightSizing.java" "$src/WidgetBreakSequence.java" android-widget/ci/browser/*.java
java -cp smoke-browser/classes com.wokgui.schedulewidget.ExportUi smoke-browser/chunks
java -cp smoke-browser/classes com.wokgui.schedulewidget.CondensedRowSizingTest
java -cp smoke-browser/classes com.wokgui.schedulewidget.WidgetAutoLayoutSizingTest
java -cp smoke-browser/classes com.wokgui.schedulewidget.WidgetHeightSizingTest
java -cp smoke-browser/classes com.wokgui.schedulewidget.WidgetBreakSequenceTest

EDT_UI_CHUNKS=smoke-browser/chunks timeout 180s node android-widget/ci/browser/verify.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_feedback_672.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_678.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_ui_679.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_ui_759.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_ui_760.cjs
