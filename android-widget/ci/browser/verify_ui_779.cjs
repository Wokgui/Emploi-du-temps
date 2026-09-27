const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');

const root=path.resolve(__dirname,'../..');
const read=relative=>fs.readFileSync(path.join(root,relative),'utf8');
const provider=read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetProvider.java');
const condensed=read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
const sizing=read('app/src/main/java/com/wokgui/schedulewidget/WidgetHeightSizing.java');
const adaptiveLayout=read('app/src/main/res/layout/widget_schedule_adaptive.xml');
const adaptiveRow=read('app/src/main/res/layout/widget_adaptive_course_row.xml');
const condensedRow=read('app/src/main/res/layout/widget_adaptive_condensed_row.xml');
const gradle=read('app/build.gradle');

assert.match(provider,/condensedAdaptive\?R[.]layout[.]widget_schedule_adaptive:R[.]layout[.]widget_schedule/);
assert.match(provider,/format==WidgetLayoutStore[.]FORMAT_CONDENSED[\s\S]*CondensedCoursesService[.]buildAdaptiveRows/);
assert.match(condensed,/buildAdaptiveRows\(Context context, int widgetId\)/);
assert.match(condensed,/factory[.]createViewAt\(i, true\)/);
assert.match(condensed,/WidgetHeightSizing[.]smallestHeightDp/);
assert.match(condensed,/if \(adaptiveHost\)[\s\S]*rowRoot, -1, TypedValue[.]COMPLEX_UNIT_PX/);
assert.doesNotMatch(condensed,/setViewLayoutHeight\(R[.]id[.]adaptiveRowSlot/);
assert.match(sizing,/static int smallestHeightDp/);
assert.match(adaptiveRow,/android:layout_height="0dp"/);
assert.match(adaptiveRow,/android:layout_weight="1"/);
assert.match(condensed,/R[.]layout[.]widget_adaptive_condensed_row/);
assert.match(condensed,/if \(!adaptiveHost\)[\s\S]*setTextViewTextSize/);
assert.match(condensedRow,/android:layout_height="0dp"/);
assert.match(condensedRow,/android:layout_weight="1"/);
assert.ok((condensedRow.match(/android:autoSizeTextType="uniform"/g)||[]).length>=2);
assert.match(condensedRow,/android:autoSizeMinTextSize="3sp"/);
assert.match(adaptiveLayout,/android:id="@\+id\/adaptiveDayRows"/);
assert.doesNotMatch(adaptiveLayout,/ListView|upcomingList|setRemoteAdapter/);
assert.match(gradle,/versionCode 779001/);
assert.match(gradle,/versionName '7[.]79'/);

console.log('ui_779_condensed_uses_real_weighted_host_height=passed');
console.log('ui_779_condensed_adaptive_layout_has_no_list_owner=passed');
console.log('ui_779_all_rows_share_visible_height_at_every_widget_size=passed');
console.log('ui_779_condensed_text_auto_sizes_from_real_row_height=passed');
