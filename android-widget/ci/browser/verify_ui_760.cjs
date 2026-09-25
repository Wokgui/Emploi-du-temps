const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const settle = page => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));

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
    const data = {
      AdvancedSettings: '{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true}',
      UiSettings: '{"language":"fr","theme":"blue","appFontScale":1,"widgetFontScale":1}'
    };
    window.__uiData760 = data;
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
        if (key === 'listProfiles') return '{"current":"main","profiles":[{"id":"main","name":"Principal"}]}';
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
  await page.waitForFunction(() => window.__calendarNavigation757 && window.__feedback663 && window.__settingsLayoutV1);
  await settle(page);

  assert.equal(await page.locator('#todayTitle').textContent(), 'Jeudi le 1er octobre 2026');
  assert.equal(await page.locator('#todayDate').textContent(), 'Semaine A');
  await page.screenshot({ path: path.resolve('smoke-browser/ui-760-today.png'), fullPage: false });

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true');
  const breaks = page.locator('#breakSettings86');
  if (!(await breaks.evaluate(node => node.open))) await breaks.locator(':scope > summary').tap();
  await page.waitForFunction(() => document.getElementById('breakSettings86').open);
  const openState = await page.evaluate(() => {
    const box = document.getElementById('breakDisplaySetting');
    return {
      parent: box.closest('.settingsSection86')?.id,
      visibility: getComputedStyle(box).visibility,
      matrix: getComputedStyle(document.querySelector('#breakDisplaySetting .feedback663Matrix')).display,
      height: box.getBoundingClientRect().height
    };
  });
  assert.equal(openState.parent, 'breakSettings86', JSON.stringify(openState));
  assert.equal(openState.visibility, 'visible', JSON.stringify(openState));
  assert.equal(openState.matrix, 'grid', JSON.stringify(openState));
  assert.ok(openState.height > 130, JSON.stringify(openState));

  const settingsTitles = await page.evaluate(() => {
    document.getElementById('advancedSettings85').open = true;
    const summaries = [...document.querySelectorAll('#settingsSheet > details > summary')];
    const mainSize = parseFloat(getComputedStyle(document.querySelector('#breakSettings86 > summary')).fontSize);
    const requested = ['#week658LunchSettings .w658Title'];
    const advanced = ['#advReminderTitle','#advExceptionsTitle','#advProfilesTitle','#advBackupTitle'];
    const widget = document.getElementById('widgetSettings86'), widgetSummary = widget.querySelector(':scope > summary');
    return {
      mainSize,
      summarySizes: summaries.map(node => ({ text: node.textContent.trim(), size: parseFloat(getComputedStyle(node).fontSize) })),
      requestedSizes: requested.map(selector => ({ selector, size: parseFloat(getComputedStyle(document.querySelector(selector)).fontSize) })),
      subcategorySize: parseFloat(getComputedStyle(document.getElementById('advRangeTitle')).fontSize),
      advancedSizes: advanced.map(selector => ({ selector, size: parseFloat(getComputedStyle(document.querySelector(selector)).fontSize) })),
      schoolSize: parseFloat(getComputedStyle(document.getElementById('schoolTitle')).fontSize),
      calendarDisplay: getComputedStyle(document.getElementById('advCalendarTitle')).display,
      widgetDisplay: getComputedStyle(widgetSummary).display,
      widgetHeight: widget.getBoundingClientRect().height,
      widgetText: widgetSummary.textContent.trim()
    };
  });
  assert.ok(settingsTitles.mainSize >= 13, JSON.stringify(settingsTitles));
  settingsTitles.summarySizes.forEach(title => assert.ok(Math.abs(title.size - settingsTitles.mainSize) <= 0.1, JSON.stringify(settingsTitles)));
  settingsTitles.requestedSizes.forEach(title => assert.ok(Math.abs(title.size - settingsTitles.subcategorySize) <= 0.1, JSON.stringify(settingsTitles)));
  settingsTitles.advancedSizes.forEach(title => assert.ok(Math.abs(title.size - settingsTitles.schoolSize) <= 0.1, JSON.stringify(settingsTitles)));
  assert.equal(settingsTitles.calendarDisplay, 'none', JSON.stringify(settingsTitles));
  assert.equal(settingsTitles.widgetDisplay, 'list-item', JSON.stringify(settingsTitles));
  assert.ok(settingsTitles.widgetHeight > 30, JSON.stringify(settingsTitles));
  assert.equal(settingsTitles.widgetText, 'Affichage du widget', JSON.stringify(settingsTitles));
  await page.evaluate(() => document.querySelectorAll('#settingsSheet > details').forEach(node => { node.open = false; }));
  await settle(page);
  await page.screenshot({ path: path.resolve('smoke-browser/ui-762-settings.png'), fullPage: false });

  await page.locator('#settingsDone').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'false');
  await settle(page);

  for (const mode of ['today', 'week', 'edit', 'today']) {
    await page.locator('.nav[data-mode="' + mode + '"]').tap();
    await settle(page);
    const hidden = await page.evaluate(() => {
      const modal = document.getElementById('settingsModal');
      const box = document.getElementById('breakDisplaySetting');
      const matrix = document.querySelector('#breakDisplaySetting .feedback663Matrix');
      return {
        modalOpen: modal.getAttribute('data-edt-open'),
        modalVisibility: getComputedStyle(modal).visibility,
        boxVisibility: getComputedStyle(box).visibility,
        matrixVisibility: getComputedStyle(matrix).visibility,
        parent: box.closest('.settingsSection86')?.id
      };
    });
    assert.equal(hidden.modalOpen, 'false', JSON.stringify(hidden));
    assert.equal(hidden.modalVisibility, 'hidden', JSON.stringify(hidden));
    assert.equal(hidden.boxVisibility, 'hidden', JSON.stringify(hidden));
    assert.equal(hidden.matrixVisibility, 'hidden', JSON.stringify(hidden));
    assert.equal(hidden.parent, 'breakSettings86', JSON.stringify(hidden));
  }

  const stress = await page.evaluate(async () => {
    const pause = () => new Promise(resolve => setTimeout(resolve, 25));
    let leaks = 0;
    for (let i = 0; i < 24; i++) {
      window.__edtHeavyPanels648.openSettings();
      document.getElementById('breakSettings86').open = true;
      await pause();
      window.__edtHeavyPanels648.closeSettings();
      document.querySelector('.nav[data-mode="' + (i % 2 ? 'today' : 'week') + '"]').click();
      await pause();
      const box = document.getElementById('breakDisplaySetting');
      if (getComputedStyle(box).visibility !== 'hidden' || box.closest('.settingsSection86')?.id !== 'breakSettings86') leaks++;
    }
    return { leaks, parent: document.getElementById('breakDisplaySetting').closest('.settingsSection86')?.id };
  });
  assert.deepEqual(stress, { leaks: 0, parent: 'breakSettings86' });

  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await settle(page);
  const rails = await page.evaluate(() => {
    if (window.fitActiveWeek658) window.fitActiveWeek658();
    const grid = document.getElementById('weekGrid');
    const gridRect = grid.getBoundingClientRect();
    const lunchTimes = [...grid.querySelectorAll(':scope > .wh.timecol.week658LunchTime')];
    const lunchTop = lunchTimes[0]?.getBoundingClientRect().top;
    const lunchBottom = lunchTimes[lunchTimes.length - 1]?.getBoundingClientRect().bottom;
    return [...grid.querySelectorAll(':scope > .week658LunchRail')].map(node => {
      const rect = node.getBoundingClientRect(), style = getComputedStyle(node);
      const boundary = node.classList.contains('week658LunchRailTop') ? lunchTop : lunchBottom;
      return {
        leftDelta: Math.abs(rect.left - gridRect.left),
        rightDelta: Math.abs(rect.right - gridRect.right),
        widthDelta: Math.abs(rect.width - gridRect.width),
        height: rect.height,
        boundaryDelta: Math.abs(rect.bottom - boundary),
        background: style.backgroundColor,
        zIndex: Number(style.zIndex)
      };
    });
  });
  assert.ok(rails.length >= 2, JSON.stringify(rails));
  rails.forEach(rail => {
    assert.ok(rail.leftDelta <= 0.5, JSON.stringify(rail));
    assert.ok(rail.rightDelta <= 0.5, JSON.stringify(rail));
    assert.ok(rail.widthDelta <= 0.5, JSON.stringify(rail));
    assert.equal(rail.height, 1, JSON.stringify(rail));
    assert.ok(rail.boundaryDelta <= 0.25, JSON.stringify(rail));
    assert.notEqual(rail.background, 'rgba(0, 0, 0, 0)', JSON.stringify(rail));
    assert.ok(rail.zIndex >= 80, JSON.stringify(rail));
  });
  await page.screenshot({ path: path.resolve('smoke-browser/ui-762-week.png'), fullPage: false });

  assert.deepEqual(errors, []);
  console.log('ui_760_break_matrix_never_leaks=passed');
  console.log('ui_760_settings_layout_has_single_owner=passed');
  console.log('ui_760_day_header_two_lines=passed');
  console.log('ui_760_lunch_lines_span_full_grid=passed');
  console.log('ui_762_settings_titles_and_widget_section=passed');
  console.log('ui_762_lunch_lines_match_grid_boundaries=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
