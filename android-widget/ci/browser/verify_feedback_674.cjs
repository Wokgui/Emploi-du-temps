const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8');
const frames = count => new Promise(resolve => {
  const next = left => left <= 0 ? resolve() : requestAnimationFrame(() => next(left - 1));
  next(count);
});

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
  await page.waitForFunction(() => window.__feedback674 && window.__edtFastInteractionV3 && typeof window.fitActiveWeek674 === 'function');

  await page.locator('.nav[data-mode="today"]').tap();
  await page.waitForFunction(() => document.getElementById('viewToday').classList.contains('active') && document.querySelector('.todayCourse'));
  const gestures = await page.evaluate(async () => {
    const row = document.querySelector('.todayCourse');
    window.__opens674 = 0;
    row.onclick = () => { window.__opens674++; };
    const wait = ms => new Promise(resolve => setTimeout(resolve, ms));
    const paint = () => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    const run = async (delay, move) => {
      const base = { bubbles: true, cancelable: true, pointerId: 74, pointerType: 'touch', isPrimary: true, clientX: 100, clientY: 300 };
      row.dispatchEvent(new PointerEvent('pointerdown', base));
      if (move) row.dispatchEvent(new PointerEvent('pointermove', { ...base, clientY: 342 }));
      await wait(delay);
      row.dispatchEvent(new PointerEvent('pointerup', { ...base, clientY: move ? 342 : 300 }));
      row.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, clientX: 100, clientY: move ? 342 : 300 }));
      await paint();
      return window.__opens674;
    };
    return {
      quick: await run(35, false),
      long: await run(380, false),
      moved: await run(35, true),
      blocked: window.__edtHeavyInputOwner648.scrollClicksBlocked,
      todayTouch: getComputedStyle(row).touchAction
    };
  });
  assert.equal(gestures.quick, 1, JSON.stringify(gestures));
  assert.equal(gestures.long, 1, JSON.stringify(gestures));
  assert.equal(gestures.moved, 1, JSON.stringify(gestures));
  assert.equal(gestures.blocked, 2, JSON.stringify(gestures));
  assert.match(gestures.todayTouch, /pan-y/);

  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  await page.evaluate(() => {
    const makeDay = prefix => ({ enabled: true, courses: [
      { label: prefix + '-08', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: prefix + '-10', room: '102', start: '10:00', end: '11:00', slot: 3 },
      { label: prefix + '-13', room: '201', start: '13:00', end: '14:00', slot: 6 }
    ] });
    weeks.A[2] = makeDay('EDIT-A'); weeks.B[2] = makeDay('EDIT-B'); activeWeek = 'A'; selected = 2; renderEdit();
  });
  await page.waitForFunction(() => document.getElementById('editList').innerText.includes('EDIT-A'));
  const editTouch = await page.locator('#editList .editCourse').first().evaluate(node => getComputedStyle(node).touchAction);
  assert.match(editTouch, /pan-y/);

  const transition = await page.evaluate(() => new Promise(resolve => {
    const bottom = document.querySelector('.bottom');
    const before = { text: bottom.innerText, active: bottom.querySelector('.nav.active')?.dataset.mode };
    document.querySelector('#weekTabs .weekTab[data-week="B"]').click();
    requestAnimationFrame(() => requestAnimationFrame(() => {
      const cover = document.querySelector('.editSwapCover673');
      resolve({
        cover: !!cover,
        coverZ: cover ? Number(getComputedStyle(cover).zIndex) : 0,
        bottomZ: Number(getComputedStyle(bottom).zIndex),
        text: bottom.innerText,
        active: bottom.querySelector('.nav.active')?.dataset.mode,
        before
      });
    }));
  }));
  assert.equal(transition.cover, true, JSON.stringify(transition));
  assert.ok(transition.coverZ < transition.bottomZ, JSON.stringify(transition));
  assert.equal(transition.text, transition.before.text);
  assert.equal(transition.active, 'edit');
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));

  await page.evaluate(() => { document.querySelector('main.wrap').style.height = '1800px'; });
  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));
  const weekFit = await page.evaluate(() => {
    const stage = document.querySelector('main.wrap'), view = document.getElementById('viewWeek'), grid = document.querySelector('#weekGrid .wc');
    const css = getComputedStyle(stage), top = parseFloat(css.paddingTop) || 0, bottom = parseFloat(css.paddingBottom) || 0;
    return {
      actual: stage.getBoundingClientRect().height,
      expected: Math.ceil(Math.max(view.offsetHeight, view.scrollHeight) + top + bottom),
      boxSizing: css.boxSizing,
      tight: stage.classList.contains('edtWeekTight674'),
      weekTouch: grid ? getComputedStyle(grid).touchAction : ''
    };
  });
  assert.equal(weekFit.boxSizing, 'border-box', JSON.stringify(weekFit));
  assert.equal(weekFit.tight, true, JSON.stringify(weekFit));
  assert.ok(Math.abs(weekFit.actual - weekFit.expected) <= 1, JSON.stringify(weekFit));
  assert.match(weekFit.weekTouch, /pan-y/);

  const rowXml = read('app/src/main/res/layout/widget_course_row.xml');
  const standardPreview = read('app/src/main/res/layout/widget_preview.xml');
  const standard = read('app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java');
  const condensed = read('app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java');
  const condensedInfo = read('app/src/main/res/xml/widget_info_condensed.xml');
  const fast = read('app/src/main/java/com/wokgui/schedulewidget/FastInteractionUi.java');
  const feedback = read('app/src/main/java/com/wokgui/schedulewidget/Feedback674Ui.java');
  const priorFeedback = read('app/src/main/java/com/wokgui/schedulewidget/Feedback673Ui.java');
  const gradle = read('app/build.gradle');
  assert.match(rowXml, /android:id="@\+id\/rowStartTime"/);
  assert.match(standardPreview, /android:text="08:00"/);
  assert.match(standard, /setTextViewText\(R\.id\.rowStartTime/);
  assert.match(standard, /setOnClickFillInIntent\(R\.id\.rowStartTime/);
  assert.match(standard, /private String startTime\(String range\)/);
  assert.match(condensedInfo, /android:minHeight="180dp"/);
  assert.match(condensedInfo, /android:targetCellHeight="3"/);
  assert.match(condensedInfo, /android:minResizeHeight="40dp"/);
  assert.ok((condensed.match(/return 180;/g) || []).length >= 2);
  assert.match(fast, /scrollClicksBlocked/);
  assert.match(read('app/src/main/java/com/wokgui/schedulewidget/HeavyPanelUi648.java'), /pendingCourse/);
  assert.match(fast, /g\.duration>320/);
  assert.match(fast, /touch-action:pan-y pinch-zoom/);
  assert.match(feedback, /box-sizing:border-box!important/);
  assert.match(feedback, /edtWeekTight674/);
  assert.match(priorFeedback, /cover\.style\.zIndex='11'/);
  assert.match(gradle, /versionCode 674001/);
  assert.match(gradle, /versionName '6\.74'/);
  assert.deepEqual(errors, []);
  console.log('feedback_674_standard_widget_left_time=passed');
  console.log('feedback_674_condensed_default_three_rows=passed');
  console.log('feedback_674_scroll_gesture_does_not_open_course=passed');
  console.log('feedback_674_edit_bottom_bar_stable=passed');
  console.log('feedback_674_week_has_no_extra_blank_scroll=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });