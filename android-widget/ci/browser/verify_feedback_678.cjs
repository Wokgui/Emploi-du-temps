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
    const NativeDate = Date;
    const fixedNow = NativeDate.parse('2026-10-01T12:00:00Z');
    class FixedDate extends NativeDate {
      constructor(...args) { super(...(args.length ? args : [fixedNow])); }
      static now() { return fixedNow; }
    }
    Object.setPrototypeOf(FixedDate, NativeDate);
    window.Date = FixedDate;
    const data = { AdvancedSettings: '{"cycleLength":2,"singleWeek":false,"density":"normal","widgetAutoDensity":true}', UiSettings: '{"language":"fr","theme":"blue"}' };
    window.confirm = () => true;
    window.alert = () => {};
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (typeof key !== 'string') return undefined;
      return (...args) => {
        if (key.startsWith('save')) { data[key.slice(4)] = args[0]; return true; }
        if (key === 'setCurrentWeek') { data.CurrentWeek = args[0]; return true; }
        if (key === 'loadSchedule') return data.Schedule || '';
        if (key === 'loadUiSettings') return data.UiSettings;
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings;
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
  await page.waitForFunction(() => window.__feedback678 && typeof window.navigateStable678 === 'function');
  await settle(page);

  const initial = await page.evaluate(() => ({
    contextDisplay: getComputedStyle(document.querySelector('.contextBar')).display,
    editModeDisplay: getComputedStyle(document.getElementById('weekModeBar')).display,
    settingsControls: document.querySelectorAll('#settingsSheet #settingsWeekCycle678').length,
    cycleOutsideSettings: [...document.querySelectorAll('.weekCycleChoice678')].filter(node => !node.closest('#settingsSheet')).length,
    cycle: [...document.querySelectorAll('.weekCycleChoice678')].map(node => ({ value: node.dataset.count, active: node.classList.contains('active') })),
    today: document.getElementById('todayTitle').textContent
  }));
  assert.equal(initial.contextDisplay, 'none', JSON.stringify(initial));
  assert.equal(initial.editModeDisplay, 'none', JSON.stringify(initial));
  assert.equal(initial.settingsControls, 1, JSON.stringify(initial));
  assert.equal(initial.cycleOutsideSettings, 0, JSON.stringify(initial));
  assert.equal(initial.cycle.find(item => item.value === '2').active, true, JSON.stringify(initial));
  assert.equal(initial.today, 'Jeudi le 1er octobre 2026');
  assert.equal(await page.locator('#todayDate').textContent(), 'Semaine A');

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true' || document.getElementById('settingsModal').classList.contains('show'));
  await page.screenshot({ path: path.resolve('smoke-browser/feedback-678-settings.png'), fullPage: false });
  if (!(await page.locator('#weekTypeSettings86').evaluate(el => el.open))) await page.locator('#weekTypeSettings86 > summary').tap();
  await page.waitForFunction(() => document.getElementById('weekTypeSettings86')?.open === true);
  await page.locator('.weekCurrentChoice678[data-week="B"]').tap();
  await page.waitForFunction(() => /semaine B/i.test(document.getElementById('todayDate').textContent));
  await page.locator('#settingsDone').tap();
  await settle(page);
  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await settle(page);
  assert.match(await page.locator('#viewWeek .weekTop h2').textContent(), /Semaine du/i);
  assert.match(await page.locator('#weekCycleLabel757').textContent(), /Semaine B/i);
  await page.screenshot({ path: path.resolve('smoke-browser/feedback-678-week.png'), fullPage: false });

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true' || document.getElementById('settingsModal').classList.contains('show'));
  if (!(await page.locator('#weekTypeSettings86').evaluate(el => el.open))) await page.locator('#weekTypeSettings86 > summary').tap();
  await page.waitForFunction(() => document.getElementById('weekTypeSettings86')?.open === true);
  await page.locator('.weekCycleChoice678[data-count="1"]').tap();
  await page.waitForFunction(() => document.documentElement.classList.contains('singleWeek678'));
  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="today"]').tap();
  await settle(page);
  const single = await page.evaluate(() => ({ today: document.getElementById('todayTitle').textContent, week: document.querySelector('#viewWeek .weekTop h2').textContent, currentRow: getComputedStyle(document.querySelector('.weekCurrentSettings678')).display }));
  assert.doesNotMatch(single.today, /Jour [A-D]|Semaine [A-D]/, JSON.stringify(single));
  assert.doesNotMatch(single.week, /Semaine [A-D]/, JSON.stringify(single));
  assert.equal(single.currentRow, 'none', JSON.stringify(single));

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true' || document.getElementById('settingsModal').classList.contains('show'));
  if (!(await page.locator('#weekTypeSettings86').evaluate(el => el.open))) await page.locator('#weekTypeSettings86 > summary').tap();
  await page.waitForFunction(() => document.getElementById('weekTypeSettings86')?.open === true);
  await page.locator('.weekCycleChoice678[data-count="2"]').tap();
  await page.waitForFunction(() => !document.documentElement.classList.contains('singleWeek678'));
  await page.locator('.weekCurrentChoice678[data-week="A"]').tap();
  await page.locator('#settingsDone').tap();
  await settle(page);

  const stress = await page.evaluate(async () => {
    const frame = () => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    const sample = { days: [2], parsed: { 2: [{ start: '08:00', end: '09:00', label: 'Cours importé', room: '217', confidence: .98 }] }, count: 1, quality: 'high', warningCodes: [] };
    let failures = 0;
    for (let i = 0; i < 360; i++) {
      if (i % 30 === 0) {
        window.openTimetableImportReview(sample);
        await frame();
        document.querySelector('#edtImportReview .irApply').click();
        await frame();
        if (document.getElementById('edtImportReview').classList.contains('show')) failures++;
      }
      const target = ['today', 'week', 'edit'][i % 3];
      document.querySelector('.nav[data-mode="' + target + '"]').click();
      await frame();
      const view = document.getElementById('view' + target.charAt(0).toUpperCase() + target.slice(1));
      if (!view.classList.contains('active')) failures++;
    }
    document.querySelector('.nav[data-mode="week"]').click();
    await frame();
    return {
      failures,
      active: document.querySelector('.view.active')?.id,
      gridCells: document.getElementById('weekGrid').children.length,
      covers: document.querySelectorAll('.weekSwapCover669').length,
      busy: document.body.classList.contains('cycle69Busy') || document.body.classList.contains('cycleSwitchBusy'),
      reviewCount: document.querySelectorAll('#edtImportReview').length,
      reviewOpen: document.getElementById('edtImportReview').classList.contains('show'),
      navPointer: getComputedStyle(document.querySelector('.nav[data-mode="week"]')).pointerEvents,
      current: typeof currentWeek === 'undefined' ? '' : currentWeek,
      activeWeek: typeof activeWeek === 'undefined' ? '' : activeWeek
    };
  });
  assert.equal(stress.failures, 0, JSON.stringify(stress));
  assert.equal(stress.active, 'viewWeek', JSON.stringify(stress));
  assert.ok(stress.gridCells > 10, JSON.stringify(stress));
  assert.equal(stress.covers, 0, JSON.stringify(stress));
  assert.equal(stress.busy, false, JSON.stringify(stress));
  assert.equal(stress.reviewCount, 1, JSON.stringify(stress));
  assert.equal(stress.reviewOpen, false, JSON.stringify(stress));
  assert.equal(stress.navPointer, 'auto', JSON.stringify(stress));
  assert.equal(stress.current, stress.activeWeek, JSON.stringify(stress));

  const styles31 = read('app/src/main/res/values-v31/styles.xml');
  const splash = read('app/src/main/res/drawable-v31/ic_splash_mark_safe.xml');
  const gradle = read('app/build.gradle');
  const chunkSource = read('app/src/main/java/com/wokgui/schedulewidget/ChunkedUiScripts.java');
  assert.match(styles31, /windowSplashScreenAnimatedIcon">@drawable\/ic_splash_mark_safe/);
  assert.match(splash, /android:insetLeft="24dp"[\s\S]*android:insetBottom="24dp"/);
  assert.match(chunkSource, /Feedback678Ui\.script\(\)/);
  assert.match(gradle, /versionCode 760001/);
  assert.match(gradle, /versionName '7[.]60'/);
  assert.deepEqual(errors, []);
  console.log('feedback_678_week_type_is_settings_only=passed');
  console.log('feedback_678_day_and_week_labels_follow_cycle=passed');
  console.log('feedback_678_single_week_hides_cycle_mentions=passed');
  console.log('feedback_678_import_and_navigation_360_cycles=passed');
  console.log('feedback_678_android12_splash_icon_is_inset=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
