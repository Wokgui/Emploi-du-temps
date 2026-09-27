const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');

const root=path.resolve(__dirname,'../..');
const read=relative=>fs.readFileSync(path.join(root,relative),'utf8');

const provider=read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetProvider.java');
const condensed=read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
const gradle=read('app/build.gradle');

assert.match(provider,/automaticDensity = AdvancedSettingsStore[.]widgetAutoDensity\(context\)/);
assert.match(provider,/adaptiveRows = automaticDensity[\s\S]{0,100}format != WidgetLayoutStore[.]FORMAT_MINI[\s\S]{0,100}format != WidgetLayoutStore[.]FORMAT_CONDENSED/);
assert.doesNotMatch(provider,/CondensedCoursesService[.]buildAdaptiveRows/);
assert.match(provider,/List<RemoteViews> rows = UpcomingCoursesService[.]buildAdaptiveRows/);

assert.doesNotMatch(condensed,/buildAdaptiveRows|adaptiveHost|adaptiveRowSlot/);
assert.match(condensed,/boolean courseItem = item[.]type == Item[.]COURSE/);
assert.match(condensed,/rowCondensedLineTop,[\s\S]{0,100}courseItem && position > 0 \? View[.]VISIBLE : View[.]GONE/);
assert.match(condensed,/rowCondensedLineBottom,[\s\S]{0,120}courseItem && position < items[.]size\(\) - 1 \? View[.]VISIBLE : View[.]GONE/);
const nonCourse=condensed.match(/if \(item[.]type != Item[.]COURSE\) \{[\s\S]*?\} else \{/);
assert.ok(nonCourse);assert.match(nonCourse[0],/setViewVisibility\(R[.]id[.]rowCondensedAccent, View[.]GONE\)/);
assert.match(gradle,/versionCode 777001/);assert.match(gradle,/versionName '7[.]77'/);

console.log('ui_776_condensed_single_renderer=passed');
console.log('ui_776_break_timeline_and_accent_hidden=passed');
