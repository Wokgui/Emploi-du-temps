const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');

(async () => {
  const browser = await chromium.launch({ headless: true, ...(process.env.EDT_BROWSER_CHANNEL ? { channel: process.env.EDT_BROWSER_CHANNEL } : {}) });
  const context = await browser.newContext({ viewport: { width: 412, height: 600 }, isMobile: true, hasTouch: true });
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
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{"density":"normal","showBreaksWeek":true,"showLunchWeek":true}';
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
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));
  const fit = await page.evaluate(() => {
    window.fitActiveWeek676();
    const grid = document.getElementById('weekGrid'), scroller = document.querySelector('#viewWeek .weekScroller');
    const bottom = document.querySelector('.bottom'), stage = document.querySelector('main.wrap');
    return {
      scrollY,
      rootLocked: document.documentElement.classList.contains('edtWeekFill676'),
      bodyLocked: document.body.classList.contains('edtWeekFill676'),
      stageFit: stage.classList.contains('edtWeekScreen676'),
      bodyOverflow: getComputedStyle(document.body).overflowY,
      rootOverflow: getComputedStyle(document.documentElement).overflowY,
      gridBottom: grid.getBoundingClientRect().bottom,
      scrollerBottom: scroller.getBoundingClientRect().bottom,
      navTop: bottom.getBoundingClientRect().top,
      rowHeight: grid.style.getPropertyValue('--week676-grid-height'),
      stagePaddingBottom: parseFloat(getComputedStyle(stage).paddingBottom) || 0,
      documentHeight: Math.max(document.body.scrollHeight, document.documentElement.scrollHeight),
      viewport: innerHeight
    };
  });
  assert.equal(fit.scrollY, 0, JSON.stringify(fit));
  assert.equal(fit.rootLocked, true, JSON.stringify(fit));
  assert.equal(fit.bodyLocked, true, JSON.stringify(fit));
  assert.equal(fit.stageFit, true, JSON.stringify(fit));
  assert.equal(fit.bodyOverflow, 'hidden', JSON.stringify(fit));
  assert.equal(fit.rootOverflow, 'hidden', JSON.stringify(fit));
  assert.ok(fit.gridBottom <= fit.navTop + 1, JSON.stringify(fit));
  assert.ok(fit.scrollerBottom <= fit.navTop + 1, JSON.stringify(fit));
  assert.equal(fit.stagePaddingBottom, 0, JSON.stringify(fit));
  await page.mouse.wheel(0, 800);
  await page.waitForTimeout(80);
  assert.equal(await page.evaluate(() => scrollY), 0, 'Week view must ignore downward page scrolling');

  const rowXml = read('app/src/main/res/layout/widget_course_row.xml');
  const miniPreview = read('app/src/main/res/layout/widget_preview_mini.xml');
  const condensedPreview = read('app/src/main/res/layout/widget_preview_condensed.xml');
  const service = read('app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java');
  const feedback = read('app/src/main/java/com/wokgui/schedulewidget/Feedback676Ui.java');
  const gradle = read('app/build.gradle');
  assert.match(rowXml, /rowMiniTitle[\s\S]*android:singleLine="true"[\s\S]*android:ellipsize="end"/);
  assert.match(miniPreview, /android:text="Emploi du temps"[\s\S]*android:textSize="7sp"/);
  assert.match(miniPreview, /android:text="14\/09 · A"/);
  assert.match(service, /widgetWidth > 0 && widgetWidth <= 140/);
  assert.match(service, /compactDateLabel\(targetDate\)/);
  assert.equal((condensedPreview.match(/android:layout_height="0dp"\s+android:layout_weight="1"\s+android:orientation="horizontal"/g) || []).length, 7);
  assert.doesNotMatch(condensedPreview, /android:gravity="center_vertical"\s+android:padding="0dp"/);
  assert.match(feedback, /overflow-y:hidden!important/);
  assert.match(feedback, /--week676-grid-height/);
  assert.match(gradle, /versionCode 678001/);
  assert.match(gradle, /versionName '6\.78'/);
  assert.deepEqual(errors, []);
  console.log('feedback_675_mini_header_adapts=passed');
  console.log('feedback_675_condensed_preview_fills_height=passed');
  console.log('feedback_675_week_locked_and_fits_viewport=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
