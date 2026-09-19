const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const settle = page => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));

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
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{"density":"normal","widgetAutoDensity":true,"showBreaksWeek":true,"showLunchWeek":true}';
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
  await page.waitForFunction(() => window.__feedback676 && typeof window.fitActiveWeek676 === 'function');
  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await settle(page);

  const first = await page.evaluate(() => {
    fitActiveWeek676();
    const grid = document.getElementById('weekGrid'), scroller = document.querySelector('#viewWeek .weekScroller'), nav = document.querySelector('.bottom');
    const rect = grid.getBoundingClientRect(), scrollRect = scroller.getBoundingClientRect(), navTop = nav.getBoundingClientRect().top;
    const columns = grid.querySelectorAll(':scope > .wh.day').length + 1;
    return {
      top: rect.top, bottom: rect.bottom, height: rect.height, scrollerHeight: scrollRect.height, navTop,
      rows: Math.ceil(grid.children.length / columns),
      locked: document.documentElement.classList.contains('edtWeekFill676') && document.body.classList.contains('edtWeekFill676'),
      stage: document.querySelector('main.wrap').classList.contains('edtWeekScreen676'),
      oldSizing: grid.style.getPropertyValue('--week675-row-height'),
      newSizing: grid.style.getPropertyValue('--week676-grid-height'),
      overflow: getComputedStyle(document.body).overflowY
    };
  });
  assert.equal(first.locked, true, JSON.stringify(first));
  assert.equal(first.stage, true, JSON.stringify(first));
  assert.equal(first.overflow, 'hidden', JSON.stringify(first));
  assert.equal(first.oldSizing, '', JSON.stringify(first));
  assert.ok(first.rows >= 8, JSON.stringify(first));
  assert.ok(Math.abs(first.height - first.scrollerHeight) <= 1, JSON.stringify(first));
  assert.ok(first.bottom <= first.navTop - 1 && first.bottom >= first.navTop - 4, JSON.stringify(first));
  assert.ok(Number.parseFloat(first.newSizing) > 0, JSON.stringify(first));

  const heights = [];
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true' || document.getElementById('settingsModal').classList.contains('show'));
  for (let i = 0; i < 20; i++) {
    await page.locator(`.weekCurrentChoice678[data-week="${i % 2 ? 'A' : 'B'}"]`).tap();
    await settle(page);
    heights.push(await page.evaluate(() => {
      fitActiveWeek676();
      const grid = document.getElementById('weekGrid'), top = [...grid.querySelectorAll(':scope > .week658LunchRowTop')], bottom = [...grid.querySelectorAll(':scope > .week658LunchRowBottom')];
      const unique = nodes => [...new Set(nodes.map(node => Math.round(node.getBoundingClientRect().top)))];
      const uniqueBottom = nodes => [...new Set(nodes.map(node => Math.round(node.getBoundingClientRect().bottom)))];
      const cover = document.querySelector('.weekSwapCover669'), gridRect = grid.getBoundingClientRect(), coverRect = cover && cover.getBoundingClientRect();
      return {
        height: Math.round(gridRect.height),
        bottom: Math.round(gridRect.bottom),
        topLines: unique(top).length,
        bottomLines: uniqueBottom(bottom).length,
        covers: document.querySelectorAll('.weekSwapCover669').length,
        coverHeight: coverRect ? Math.round(coverRect.height) : 0,
        coverBottom: coverRect ? Math.round(coverRect.bottom) : 0,
        sizing: grid.style.getPropertyValue('--week676-grid-height')
      };
    }));
  }
  await page.locator('#settingsDone').tap();
  await settle(page);
  assert.equal(new Set(heights.map(value => value.height)).size, 1, JSON.stringify(heights));
  heights.forEach(value => {
    assert.ok(value.topLines <= 1, JSON.stringify(value));
    assert.ok(value.bottomLines <= 1, JSON.stringify(value));
    assert.ok(value.covers <= 1, JSON.stringify(value));
    if (value.covers) {
      assert.equal(value.coverHeight, value.height, JSON.stringify(value));
      assert.equal(value.coverBottom, value.bottom, JSON.stringify(value));
    }
    assert.ok(Number.parseFloat(value.sizing) > 0, JSON.stringify(value));
  });
  await page.waitForTimeout(80); await settle(page);
  assert.equal(await page.locator('.weekSwapCover669').count(), 0, 'week snapshot must leave after the final paint');
  await page.screenshot({ path: path.resolve('smoke-browser/feedback-676-week.png'), fullPage: false });

  const condensedPreview = read('app/src/main/res/layout/widget_preview_condensed.xml');
  const rowXml = read('app/src/main/res/layout/widget_course_row.xml');
  const standard = read('app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java');
  const condensed = read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
  const sizing = read('app/src/main/java/com/wokgui/schedulewidget/WidgetAutoLayoutSizing.java');
  const feedback = read('app/src/main/java/com/wokgui/schedulewidget/Feedback676Ui.java');
  const gradle = read('app/build.gradle');
  assert.equal((condensedPreview.match(/android:layout_height="0dp" android:layout_weight="1" android:orientation="horizontal"/g) || []).length, 7);
  assert.match(condensedPreview, /android:text="09:00"[\s\S]*android:text="Trou"/);
  assert.match(condensedPreview, /android:text="12:00"[\s\S]*android:text="Midi"/);
  assert.match(rowXml, /rowRelative[\s\S]*android:singleLine="true"[\s\S]*android:maxLines="1"/);
  assert.match(standard, /setViewLayoutHeight\(R\.id\.adaptiveRowSlot, fittedHeight/);
  assert.match(condensed, /setViewLayoutHeight\(R\.id\.adaptiveRowSlot, fittedHeight/);
  assert.match(standard, /compactFutureLabel\(targetDate, base\)/);
  assert.match(sizing, /Math\.min\(26,/);
  assert.match(sizing, /Math\.min\(78,/);
  assert.match(feedback, /grid-auto-rows:minmax\(0,1fr\)/);
  assert.match(feedback, /refreshWeekAppearance658/);
  assert.match(gradle, /versionCode 678001/);
  assert.match(gradle, /versionName '6\.78'/);
  assert.deepEqual(errors, []);
  console.log('feedback_676_condensed_preview_is_really_compact=passed');
  console.log('feedback_676_classic_auto_density_keeps_full_day=passed');
  console.log('feedback_676_relative_pills_small_single_line=passed');
  console.log('feedback_676_week_one_stable_full_height_geometry=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
