const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const output = path.resolve('smoke-browser/out');
fs.mkdirSync(output, { recursive: true });
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const block = (xml, id) => {
  const match = xml.match(new RegExp('<ProgressBar[^>]*android:id="@\\+id/' + id + '"[^>]*/>', 's'));
  assert.ok(match, 'missing ' + id);
  return match[0];
};
const attrs = xml => Object.fromEntries([...xml.matchAll(/(?:android:)?([A-Za-z_]+)="([^"]*)"/g)].map(match => [match[1], match[2]]));

(async () => {
  const schedule = read('app/src/main/res/layout/widget_schedule.xml');
  const provider = read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetProvider.java');
  const condensedProvider = read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetCondensedProvider.java');
  const miniProvider = read('app/src/main/java/com/wokgui/schedulewidget/ScheduleWidgetMiniProvider.java');
  const store = read('app/src/main/java/com/wokgui/schedulewidget/AdvancedSettingsStore.java');
  const settings = read('app/src/main/java/com/wokgui/schedulewidget/Feedback672Ui.java');
  const sizing = read('app/src/main/java/com/wokgui/schedulewidget/CondensedRowSizing.java');
  const standard = read('app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java');
  const condensed = read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
  const gradle = read('app/build.gradle');

  const liveTop = attrs(block(schedule, 'dayProgressTop'));
  const liveBottom = attrs(block(schedule, 'dayProgress'));
  for (const key of ['style', 'layout_width', 'layout_height', 'max', 'progress', 'progressTint', 'progressBackgroundTint']) {
    assert.equal(liveTop[key], liveBottom[key], 'live bars differ on ' + key);
  }
  assert.ok(schedule.indexOf('android:id="@+id/widgetTopBar"') < schedule.indexOf('android:id="@+id/widgetBody"'));
  assert.ok(schedule.indexOf('android:id="@+id/widgetBottomBar"') > schedule.indexOf('android:id="@+id/widgetBody"'));
  for (const id of ['dayColorTop', 'dayColorBottom']) assert.match(schedule, new RegExp('@\\+id/' + id));
  assert.match(provider, /int dayProgressValue=dayProgress\(context\)/);
  assert.match(provider, /configureEdgeBar\(views,R\.id\.widgetTopBar,R\.id\.dayProgressTop,R\.id\.dayColorTop/);
  assert.match(provider, /configureEdgeBar\(views,R\.id\.widgetBottomBar,R\.id\.dayProgress,R\.id\.dayColorBottom/);
  assert.match(provider, /hidden="none"\.equals\(mode\),solid="color"\.equals\(mode\)/);
  assert.match(provider, /setBackgroundColor",color/);
  assert.match(condensedProvider, /extends ScheduleWidgetProvider/);
  assert.match(miniProvider, /extends ScheduleWidgetProvider/);

  for (const file of ['widget_preview.xml', 'widget_preview_condensed.xml', 'widget_preview_mini.xml']) {
    const xml = read('app/src/main/res/layout/' + file);
    const top = attrs(block(xml, 'previewDayProgressTop'));
    const bottom = attrs(block(xml, 'previewDayProgressBottom'));
    for (const key of ['style', 'layout_width', 'layout_height', 'max', 'progress', 'progressTint', 'progressBackgroundTint']) {
      assert.equal(top[key], bottom[key], file + ' differs on ' + key);
    }
    assert.ok(xml.indexOf('@+id/previewDayProgressTop') < xml.indexOf('@+id/previewDayProgressBottom'), file + ' bar order');
  }

  for (const key of ['widgetTopBarMode', 'widgetTopBarColor', 'widgetBottomBarMode', 'widgetBottomBarColor']) assert.match(store, new RegExp(key));
  assert.match(store, /widgetBarChromeDp/);
  assert.match(store, /"none"\.equals\(widgetTopBarMode\(context\)\)/);
  assert.match(sizing, /autoRowHeightDp\(int widgetHeightDp, int itemCount, int position, int progressChromeDp\)/);
  assert.match(standard, /AdvancedSettingsStore\.widgetBarChromeDp\(context\)/);
  assert.match(condensed, /AdvancedSettingsStore\.widgetBarChromeDp\(context\)/);
  assert.match(settings, /value="progress"/);
  assert.match(settings, /value="color"/);
  assert.match(settings, /value="none"/);
  assert.match(settings, /type="color"/);
  assert.match(gradle, /versionCode 674001/);
  assert.match(gradle, /versionName '6\.74'/);

  const browser = await chromium.launch({ headless: true, ...(process.env.EDT_BROWSER_CHANNEL ? { channel: process.env.EDT_BROWSER_CHANNEL } : {}) });
  const context = await browser.newContext({ viewport: { width: 412, height: 915 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(String(error)));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  await page.addInitScript(() => {
    window.__advanced672 = { widgetTopBarMode: 'progress', widgetTopBarColor: '#1677E8', widgetBottomBarMode: 'progress', widgetBottomBarColor: '#1677E8' };
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (typeof key !== 'string') return undefined;
      return (...args) => {
        if (key === 'loadAdvancedSettings') return JSON.stringify(window.__advanced672);
        if (key === 'saveAdvancedSettings') { window.__advanced672 = JSON.parse(args[0] || '{}'); return true; }
        if (key === 'loadSchedule') return '';
        if (key === 'loadUiSettings') return '{"language":"fr","theme":"blue"}';
        if (key === 'listProfiles') return '{"current":"main","profiles":[{"id":"main","name":"Profil principal"}]}';
        if (key.startsWith('list') || key.startsWith('supported') || key === 'loadLanguagePacks' || key === 'loadEffectiveCourses') return '[]';
        return '{}';
      };
    }});
  });
  await page.goto(pathToFileURL(asset).href);
  for (const file of fs.readdirSync(chunks).sort()) {
    await page.evaluate(fs.readFileSync(path.join(chunks, file), 'utf8') + '\n//# sourceURL=' + file);
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(() => window.__feedback672 && document.getElementById('widgetEdgeBars672'));
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648 && window.__edtHeavyPanels648.isOpen('settings'));
  const initial = await page.evaluate(() => {
    const root = document.getElementById('widgetEdgeBars672');
    const title = document.getElementById('advWidgetTitle');
    const top = document.getElementById('widgetTopBarMode672');
    const bottom = document.getElementById('widgetBottomBarMode672');
    const topColor = document.getElementById('widgetTopBarColor672');
    return {
      sameBox: root.closest('.settingBox') === title.closest('.settingBox'),
      topOptions: [...top.options].map(x => [x.value, x.textContent]),
      bottomOptions: [...bottom.options].map(x => [x.value, x.textContent]),
      topValue: top.value,
      bottomValue: bottom.value,
      topColorHidden: getComputedStyle(topColor).visibility === 'hidden',
      courseProgressLabel: document.getElementById('advShowProgressLabel').textContent
    };
  });
  assert.equal(initial.sameBox, true);
  assert.deepEqual(initial.topOptions.map(x => x[0]), ['progress', 'color', 'none']);
  assert.deepEqual(initial.bottomOptions.map(x => x[0]), ['progress', 'color', 'none']);
  assert.equal(initial.topValue, 'progress');
  assert.equal(initial.bottomValue, 'progress');
  assert.equal(initial.topColorHidden, true);
  assert.equal(initial.courseProgressLabel, 'Progression du cours en cours');

  await page.locator('#widgetTopBarMode672').selectOption('color');
  await page.evaluate(() => {
    const input = document.getElementById('widgetTopBarColor672');
    input.value = '#ff00aa';
    input.dispatchEvent(new Event('change', { bubbles: true }));
  });
  await page.locator('#widgetBottomBarMode672').selectOption('none');
  const saved = await page.evaluate(() => ({
    data: window.__advanced672,
    topVisible: getComputedStyle(document.getElementById('widgetTopBarColor672')).visibility !== 'hidden',
    bottomColorDisabled: document.getElementById('widgetBottomBarColor672').disabled
  }));
  assert.equal(saved.data.widgetTopBarMode, 'color');
  assert.equal(saved.data.widgetTopBarColor, '#ff00aa');
  assert.equal(saved.data.widgetBottomBarMode, 'none');
  assert.equal(saved.topVisible, true);
  assert.equal(saved.bottomColorDisabled, true);
  await page.screenshot({ path: path.join(output, 'settings-widget-bars-672.png'), fullPage: true });
  assert.deepEqual(errors, []);
  await context.close();
  await browser.close();

  console.log('feedback_672_three_widgets_top_and_bottom_bars=passed');
  console.log('feedback_672_progress_color_none_settings=passed');
  console.log('feedback_672_dynamic_bar_height_reservation=passed');
})().catch(error => { console.error(error); process.exitCode = 1; });