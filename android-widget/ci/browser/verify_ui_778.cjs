const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');

const root=path.resolve(__dirname,'../..');
const read=relative=>fs.readFileSync(path.join(root,relative),'utf8');
const condensed=read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
const sizing=read('app/src/main/java/com/wokgui/schedulewidget/CondensedRowSizing.java');
const test=read('ci/browser/CondensedRowSizingTest.java');
const gradle=read('app/build.gradle');

assert.match(sizing,/AUTO_MIN_ROW_DP = 1/);
assert.match(sizing,/int available = Math[.]max\(AUTO_MIN_ROW_DP, widgetHeightDp - Math[.]max\(0, progressChromeDp\)\)/);
assert.doesNotMatch(condensed,/capacity|items[.]subList/);
assert.match(condensed,/Automatic fitting keeps the complete chronological list/);
assert.match(test,/completeCount = 11/);
assert.match(test,/completeUsed != compactHeight - CondensedRowSizing[.]PROGRESS_CHROME_DP/);
assert.match(gradle,/versionCode 779001/);assert.match(gradle,/versionName '7[.]79'/);

console.log('ui_778_condensed_keeps_every_automatic_row=passed');
console.log('ui_778_compact_height_is_shared_across_complete_list=passed');
