#!/usr/bin/env bash
set -euo pipefail
mkdir -p smoke-browser/classes smoke-browser/chunks
src=android-widget/app/src/main/java/com/wokgui/schedulewidget
mapfile -t sources < <(find "$src" -name '*Ui*.java' ! -name UiSettingsStore.java)
javac -encoding UTF-8 -d smoke-browser/classes "${sources[@]}" "$src/RuntimeRepair644.java" "$src/LegacyChainRepair651.java" "$src/CondensedRowSizing.java" "$src/WidgetHeightSizing.java" "$src/WidgetBreakSequence.java" android-widget/ci/browser/*.java
java -cp smoke-browser/classes com.wokgui.schedulewidget.ExportUi smoke-browser/chunks
java -cp smoke-browser/classes com.wokgui.schedulewidget.CondensedRowSizingTest
java -cp smoke-browser/classes com.wokgui.schedulewidget.WidgetHeightSizingTest
java -cp smoke-browser/classes com.wokgui.schedulewidget.WidgetBreakSequenceTest

echo 'BROWSER_SUITE|legacy|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 180s node android-widget/ci/browser/verify.cjs
echo 'BROWSER_SUITE|legacy|passed'
echo 'BROWSER_SUITE|pipeline650|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 30s node android-widget/ci/browser/verify_pipeline_650.cjs
echo 'BROWSER_SUITE|pipeline650|passed'
echo 'BROWSER_SUITE|bursts650|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 30s node android-widget/ci/browser/verify_bursts_650.cjs
echo 'BROWSER_SUITE|bursts650|passed'
echo 'BROWSER_SUITE|actions651|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_actions_651.cjs
echo 'BROWSER_SUITE|actions651|passed'
echo 'BROWSER_SUITE|rare-actions652|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_rare_actions_652.cjs
echo 'BROWSER_SUITE|rare-actions652|passed'
echo 'BROWSER_SUITE|feedback659|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 90s node android-widget/ci/browser/verify_feedback_659.cjs
echo 'BROWSER_SUITE|feedback659|passed'
echo 'BROWSER_SUITE|feedback660|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_660.cjs
echo 'BROWSER_SUITE|feedback660|passed'
echo 'BROWSER_SUITE|feedback661|start'
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_661.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_662.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_663.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_664.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_665.cjs
EDT_UI_CHUNKS=smoke-browser/chunks timeout 120s node android-widget/ci/browser/verify_feedback_666.cjs
echo 'BROWSER_SUITE|feedback661|passed'
