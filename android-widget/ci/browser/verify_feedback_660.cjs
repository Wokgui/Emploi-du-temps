const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const output = path.resolve(process.env.EDT_BROWSER_OUTPUT || 'smoke-browser');
const asset = path.resolve(__dirname, '../../app/src/main/assets/index.html');

async function openApp(browser, width, fixedIso) {
  const context = await browser.newContext({ viewport: { width, height: 915 }, deviceScaleFactor: 1, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(String(error)));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  await page.addInitScript(({ fixedIso }) => {
    const RealDate = Date;
    const stamp = new RealDate(fixedIso).getTime();
    window.Date = class extends RealDate {
      constructor(...args) { super(...(args.length ? args : [stamp])); }
      static now() { return stamp; }
      static parse(value) { return RealDate.parse(value); }
      static UTC(...args) { return RealDate.UTC(...args); }
    };
    const data = {};
    const calls = {};
    window.__bridge660 = { data, calls };
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (typeof key !== 'string') return undefined;
      return (...args) => {
        calls[key] = (calls[key] || 0) + 1;
        if (key.startsWith('save')) { data[key.slice(4)] = args[0]; return true; }
        if (key === 'exportAllSettings') return true;
        if (key === 'loadSchedule') return data.Schedule || '';
        if (key === 'loadUiSettings') return data.UiSettings || '{"language":"fr","theme":"blue"}';
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{}';
        if (key.startsWith('list') || key.startsWith('supported') || key === 'loadLanguagePacks' || key === 'loadEffectiveCourses') return '[]';
        return data[key.replace(/^load/, '')] || '{}';
      };
    }});
  }, { fixedIso });
  await page.goto(pathToFileURL(asset).href);
  for (const file of fs.readdirSync(chunks).sort()) {
    await page.evaluate(fs.readFileSync(path.join(chunks, file), 'utf8') + '\n//# sourceURL=' + file);
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(() => window.__feedback660 && window.__weekAppearance658 && window.__edtHeavyPanels648 && document.getElementById('advExportAllSettings'));
  return { context, page, errors };
}

async function verify() {
  const browser = await chromium.launch({ headless: true, channel: process.env.EDT_BROWSER_CHANNEL || 'msedge' });
  const { context, page, errors } = await openApp(browser, 412, '2026-09-19T10:00:00+02:00');
  try {
    const opening = await page.evaluate(() => ({
      mode,
      selected,
      today: todayKey(),
      activeViews: [...document.querySelectorAll('main.wrap > .view.active')].map(node => node.id),
      activeNav: document.querySelector('.nav.active')?.dataset.mode,
    }));
    assert.equal(opening.mode, 'today');
    assert.equal(opening.today, 6, 'Saturday without a weekend column must fall back to Friday');
    assert.equal(opening.selected, 6);
    assert.deepEqual(opening.activeViews, ['viewToday']);
    assert.equal(opening.activeNav, 'today');

    await page.locator('#settingsBtn').tap();
    await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
    const advanced = page.locator('#advancedSettings85');
    if (!await advanced.evaluate(element => element.open)) await page.locator('#advancedSummary85').tap();
    const settings = await page.evaluate(() => {
      const reminder = document.querySelector('label.advCheck:has(#advReminders)');
      const box = reminder.closest('.settingBox');
      const rr = reminder.getBoundingClientRect(), br = box.getBoundingClientRect();
      return {
        reminderCenter: rr.left + rr.width / 2,
        boxCenter: br.left + br.width / 2,
        startInputs: document.querySelectorAll('#week658Settings .w658Start').length,
        endInputs: document.querySelectorAll('#week658Settings .w658End').length,
        exportText: document.getElementById('advExportAllSettings').textContent.trim(),
      };
    });
    assert.ok(Math.abs(settings.reminderCenter - settings.boxCenter) < 1, 'reminder checkbox and label must be centered');
    assert.equal(settings.startInputs, 5);
    assert.equal(settings.endInputs, 5);
    assert.equal(settings.exportText, 'Exporter tous les réglages');
    await page.locator('#advExportAllSettings').evaluate(element => element.scrollIntoView({ block: 'center' }));
    await page.locator('#advExportAllSettings').tap();
    await page.waitForFunction(() => (window.__bridge660.calls.exportAllSettings || 0) === 1);
    assert.equal(await page.evaluate(() => window.__bridge660.calls.exportAllSettings || 0), 1);
    await page.screenshot({ path: path.join(output, 'settings-660-412.png') });

    const navigation = await page.evaluate(async () => {
      const frame = () => new Promise(resolve => requestAnimationFrame(resolve));
      const failures = [];
      for (let i = 0; i < 120; i++) {
        document.getElementById('settingsDone').click();
        const target = i % 2 ? 'today' : 'week';
        document.querySelector(`.nav[data-mode="${target}"]`).click();
        await frame(); await frame();
        const active = [...document.querySelectorAll('main.wrap > .view.active')];
        const view = active[0];
        if (active.length !== 1 || !view || getComputedStyle(view).visibility === 'hidden' || view.getBoundingClientRect().height <= 0) failures.push({ i, target, active: active.map(x => x.id) });
        document.getElementById('settingsBtn').click();
        await frame();
      }
      document.getElementById('settingsDone').click();
      document.querySelector('.nav[data-mode="week"]').click();
      await frame(); await frame();
      return failures;
    });
    assert.deepEqual(navigation, [], 'Settings/day/week switching must never leave a blank view');

    await page.evaluate(() => {
      weeks.A[4].courses.push({ start: '12:00', end: '13:00', label: 'COURS MIDI TEST', room: '101', slot: 0 });
      weeks.A[4].courses.sort((a, b) => min(a.start) - min(b.start));
      activeWeek = 'A';
      renderWeek();
    });
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
    await page.evaluate(() => window.refreshWeekAppearance658());
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
    const week = await page.evaluate(() => {
      const rows = [];
      const heads = [...document.querySelectorAll('#weekGrid > .wh.day')];
      const times = [...document.querySelectorAll('#weekGrid > .wh.timecol')];
      for (const time of times) {
        const found = (time.textContent || '').match(/[0-2]?[0-9]:[0-5][0-9]/g) || [];
        if (found.length < 2) continue;
        const cells = []; let node = time.nextElementSibling;
        while (node && cells.length < heads.length) { if (node.classList?.contains('wc')) cells.push(node); node = node.nextElementSibling; }
        if (cells.length === heads.length) rows.push({ time, start: found[0], end: found[1], cells });
      }
      const lunchRows = rows.filter(row => row.start === '12:00' && row.end === '13:00');
      const row = lunchRows[0];
      const styles = row ? row.cells.map(cell => getComputedStyle(cell)) : [];
      const gapVisibleTexts = [...document.querySelectorAll('#weekGrid .wc.week658Gap')].map(cell => cell.innerText.trim());
      return {
        lunchRowCount: lunchRows.length,
        timeLunch: !!row?.time.classList.contains('week658LunchTime'),
        timeBackground: row ? getComputedStyle(row.time).backgroundColor : '',
        lunchBackground: styles[0]?.backgroundColor || '',
        mondayText: row?.cells[0].innerText.trim() || '',
        tuesdayText: row?.cells[1].innerText.trim() || '',
        wednesdayText: row?.cells[2].innerText.trim() || '',
        wednesdayLunch: !!row?.cells[2].classList.contains('week658Lunch'),
        topBorder: styles[0]?.borderTopStyle || '',
        bottomBorder: styles[0]?.borderBottomStyle || '',
        gapVisibleTexts,
      };
    });
    assert.equal(week.lunchRowCount, 1, '12-13 must be a single row even when it contains a class');
    assert.equal(week.timeLunch, true, 'the left 12-13 time cell must join the lunch band');
    assert.equal(week.timeBackground, week.lunchBackground, 'time and lunch cells must use one colour');
    assert.equal(week.mondayText, 'Midi');
    assert.equal(week.tuesdayText, 'Midi');
    assert.match(week.wednesdayText, /COURS MIDI TEST/);
    assert.equal(week.wednesdayLunch, false, 'lunch must never cover a class');
    assert.equal(week.topBorder, 'solid');
    assert.equal(week.bottomBorder, 'solid');
    assert.ok(week.gapVisibleTexts.every(text => text === ''), 'free cells must not display “Trou”');
    await page.screenshot({ path: path.join(output, 'week-660-412.png') });
    assert.deepEqual(errors, [], 'JavaScript errors');
    console.log('feedback_660_opening_day=passed');
    console.log('feedback_660_navigation_120_cycles=passed');
    console.log('feedback_660_reminder_export=passed');
    console.log('feedback_660_week_lunch_and_gaps=passed');
  } finally {
    await context.close();
    await browser.close();
  }
}

verify().catch(error => { console.error(error); process.exitCode = 1; });
