const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '../..');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');

const classic = read('app/src/main/res/layout/widget_preview.xml');
const mini = read('app/src/main/res/layout/widget_preview_mini.xml');
const condensed = read('app/src/main/res/layout/widget_preview_condensed.xml');
const miniInfo = read('app/src/main/res/xml/widget_info_mini.xml');
const condensedInfo = read('app/src/main/res/xml/widget_info_condensed.xml');
const gradle = read('app/build.gradle');
const chunks = read('app/src/main/java/com/wokgui/schedulewidget/ChunkedUiScripts.java');

assert.equal((classic.match(/android:layout_height="0dp"\s+android:layout_weight="1"\s+android:orientation="horizontal"/g) || []).length, 3,
  'the three classic preview courses must share the height equally');
assert.doesNotMatch(classic, /android:layout_height="54dp"/,
  'the classic preview must not leave the remaining height to its last course');

assert.match(mini, /<FrameLayout[\s\S]*android:background="@android:color\/transparent"/);
assert.match(mini, /android:layout_height="72dp"\s+android:layout_gravity="center_vertical"/,
  'the mini preview must be a short horizontal strip');
assert.match(miniInfo, /android:minHeight="108dp"/);
assert.match(miniInfo, /android:targetCellHeight="2"/);

assert.match(condensed, /<FrameLayout[\s\S]*android:background="@android:color\/transparent"/);
assert.match(condensed, /android:layout_height="108dp"\s+android:layout_gravity="center_vertical"/,
  'the condensed preview must demonstrate compact rows');
assert.equal((condensed.match(/android:layout_height="0dp" android:layout_weight="1" android:orientation="horizontal"/g) || []).length, 7);
assert.match(condensedInfo, /android:minHeight="180dp"/);
assert.match(condensedInfo, /android:targetCellHeight="3"/);

assert.match(gradle, /versionCode 677001/);
assert.match(gradle, /versionName '6\.77'/);
assert.match(chunks, /APP_VERSION='6\.76'[^\n]*APP_VERSION='6\.77'/);
console.log('feedback_677_preview_only_scope=passed');
console.log('feedback_677_mini_preview_is_horizontal=passed');
console.log('feedback_677_classic_preview_rows_are_equal=passed');
console.log('feedback_677_condensed_preview_is_visibly_compact=passed');