const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');

const root=path.resolve(__dirname,'../..');
const read=relative=>fs.readFileSync(path.join(root,relative),'utf8');

const provider=read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetProvider.java');
const condensed=read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
const sizing=read('app/src/main/java/com/wokgui/schedulewidget/CondensedRowSizing.java');
const gradle=read('app/build.gradle');

assert.match(provider,/String adapterIdentity="edt:\/\/widget\/"\+widgetId\+"\/courses\/"\+format/);
assert.match(provider,/[?]auto="\+\(automaticDensity[?]1:0\)/);
assert.match(provider,/&density="\+AdvancedSettingsStore[.]widgetDensityPercent\(context\)/);
assert.match(provider,/&font="\+Math[.]round\(UiSettingsStore[.]widgetFontScale\(context\)[*]100f\)/);
assert.match(provider,/&chrome="\+AdvancedSettingsStore[.]widgetBarChromeDp\(context\)/);
assert.match(provider,/listIntent[.]setData\(Uri[.]parse\(adapterIdentity\)\)/);

assert.match(condensed,/automaticDensity[\s\S]{0,120}[?] CondensedRowSizing[.]autoTextScaleForRow\(fittedHeight, requestedScale\)/);
assert.match(condensed,/7f [*] effectiveScale/);
assert.match(condensed,/9f [*] effectiveScale/);
assert.match(sizing,/static float autoTextScaleForRow\(int rowHeightDp, float requestedScale\)/);
assert.match(sizing,/return Math[.]min\(requested, textScaleForRow\(rowHeightDp\)\)/);
assert.doesNotMatch(provider,/CondensedCoursesService[.]buildAdaptiveRows/);
assert.match(gradle,/versionCode 778001/);assert.match(gradle,/versionName '7[.]78'/);

console.log('ui_777_condensed_adapter_refreshes_on_text_fit_change=passed');
console.log('ui_777_condensed_auto_font_is_capped_to_row=passed');
