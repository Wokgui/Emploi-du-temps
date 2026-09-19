const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(__dirname, '../../app/src/main/assets/index.html');

(async () => {
  const browser = await chromium.launch({ headless: true, ...(process.env.EDT_BROWSER_CHANNEL ? { channel: process.env.EDT_BROWSER_CHANNEL } : {}) });
  const context = await browser.newContext({ viewport: { width: 412, height: 915 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(String(error)));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  await page.addInitScript(() => {
    const data = {};
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (typeof key !== 'string') return undefined;
      return (...args) => {
        if (key.startsWith('save')) { data[key.slice(4)] = args[0]; return true; }
        if (key === 'loadSchedule') return data.Schedule || '';
        if (key === 'loadUiSettings') return data.UiSettings || '{"language":"fr","theme":"blue"}';
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{"density":"normal","widgetDensityPercent":50,"widgetAutoDensity":true,"showBreaksWeek":true,"showLunchWeek":true}';
        if (key === 'listProfiles') return '{"current":"main","profiles":[{"id":"main","name":"Profil principal"}]}';
        if (key.startsWith('list') || key.startsWith('supported') || key === 'loadLanguagePacks' || key === 'loadEffectiveCourses') return '[]';
        return data[key.replace(/^load/, '')] || '{}';
      };
    }});
  });
  await page.goto(pathToFileURL(asset).href);
  for (const file of fs.readdirSync(chunks).sort()) {
    await page.evaluate(fs.readFileSync(path.join(chunks, file), 'utf8') + '\n//# sourceURL=' + file);
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(() => window.__feedback666);
  await page.locator('.nav[data-mode="week"]').evaluate(el => el.click());
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => {
    const makeDay = (prefix, afternoon) => ({ enabled: true, courses: [
      { label: prefix + '-08', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: prefix + '-10', room: '102', start: '10:00', end: '11:00', slot: 3 },
      { label: prefix + '-11', room: '103', start: '11:00', end: '12:00', slot: 4 },
      { label: prefix + '-' + afternoon, room: '201', start: afternoon + ':00', end: String(Number(afternoon) + 1).padStart(2, '0') + ':00', slot: 6 }
    ] });
    weeks.A[2] = makeDay('TYPE-A', '13');
    weeks.B[2] = makeDay('TYPE-B', '15');
    activeWeek = 'A';
    renderWeek();
    window.__weekGridRef668 = document.getElementById('weekGrid');
    window.__weekMutations668 = [];
    new MutationObserver(() => {
      const grid = window.__weekGridRef668;
      window.__weekMutations668.push({
        children: grid.childElementCount,
        text: grid.innerText,
        stableId: grid.id,
        visible: getComputedStyle(grid).visibility !== 'hidden'
      });
    }).observe(window.__weekGridRef668, { childList: true, subtree: true });
  });
  for (let i = 0; i < 80; i++) {
    const letter = i % 2 ? 'A' : 'B';
    await page.locator('#weekTabs .weekTab[data-week="' + letter + '"]').evaluate(el => el.click());
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))))));
    const frame = await page.evaluate(() => {
      const grid = document.getElementById('weekGrid');
      return {
        same: grid === window.__weekGridRef668,
        grids: document.querySelectorAll('.weekGrid').length,
        staging: !!document.getElementById('weekGridStable668'),
        children: grid.childElementCount,
        text: grid.innerText,
        title: document.getElementById('weekTitleLetter').textContent.trim(),
        lunch: grid.querySelectorAll('.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell').length
      };
    });
    assert.equal(frame.same, true);
    assert.equal(frame.grids, 1);
    assert.equal(frame.staging, false);
    assert.ok(frame.children > 6);
    assert.ok(frame.lunch > 0);
    assert.ok(frame.title === 'A' ? frame.text.includes('TYPE-A') && !frame.text.includes('TYPE-B') : frame.text.includes('TYPE-B') && !frame.text.includes('TYPE-A'));
  }
  const mutations = await page.evaluate(() => window.__weekMutations668);
  assert.ok(mutations.length > 0);
  assert.ok(mutations.every(frame => frame.children > 6 && frame.stableId === 'weekGrid' && frame.visible), JSON.stringify(mutations));

  const standard = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java'), 'utf8');
  const condensed = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'), 'utf8');
  const sizing = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/CondensedRowSizing.java'), 'utf8');
  const bubble = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/WidgetAutoLayoutSizing.java'), 'utf8');
  const row = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/res/layout/widget_course_row.xml'), 'utf8');
  for (const source of [standard, condensed]) {
    assert.match(source, /int firstCourse = automaticDensity \? 0 : target\.firstCourse/);
    assert.match(source, /for \(int i = firstCourse; i < courses\.size\(\); i\+\+\)/);
  }
  assert.match(standard, /setViewLayoutHeight\(R\.id\.rowRelative, WidgetAutoLayoutSizing\.pillHeightDp/);
  assert.match(standard, /setViewLayoutWidth\(R\.id\.rowRelativeBox, WidgetAutoLayoutSizing\.pillBoxWidthDp/);
  assert.match(sizing, /AUTO_MIN_ROW_DP = 1/);
  assert.match(bubble, /pillHeightDp/);
  assert.match(row, /android:id="@\+id\/rowTextBlock"/);
  assert.match(row, /android:id="@\+id\/rowRelativeBox"/);
  assert.doesNotMatch(row, /android:minWidth="60dp"/);
  assert.deepEqual(errors, []);
  console.log('feedback_668_full_day_widget_sources=passed');
  console.log('feedback_668_adaptive_bubbles=passed');
  console.log('feedback_668_atomic_week_frames=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
