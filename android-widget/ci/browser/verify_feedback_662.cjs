const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const output = path.resolve(process.env.EDT_BROWSER_OUTPUT || 'smoke-browser');
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
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{}';
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
  await page.waitForFunction(() => window.__feedback662 && window.__weekAppearance658);

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  await page.locator('#breakSettings86 > summary').tap();
  await page.locator('#gapLabel').fill('Récréation');
  await page.locator('#lunchLabel').fill('Repas');
  await page.evaluate(() => {
    for (const [id,value] of [['gapLabelWidget','Pause widget'],['lunchLabelWidget','Déjeuner widget']]) {
      const input=document.getElementById(id);input.value=value;input.dispatchEvent(new Event('input',{bubbles:true}));input.dispatchEvent(new Event('change',{bubbles:true}));
    }
  });
  await page.waitForTimeout(150);
  const editor = await page.evaluate(() => ({
    duplicateTitles: document.querySelectorAll('#viewEdit .breakNamesScope78').length,
    duplicateRows: document.querySelectorAll('#viewEdit .breakWidgetRow78').length,
    names: [...document.querySelectorAll('#breakNamesSettings763 .breakSettings>.breakRow>.breakName')].map(node => ({ text: node.textContent.trim(), align: getComputedStyle(node).textAlign })),
    advanced: JSON.parse(AndroidSchedule.loadAdvancedSettings() || '{}'),
  }));
  assert.equal(editor.duplicateTitles, 0);
  assert.equal(editor.duplicateRows, 0);
  assert.deepEqual(editor.names.map(item => item.text), ['Trou', 'Midi']);
  assert.ok(editor.names.every(item => item.align === 'center'));
  assert.equal(editor.advanced.gapWidgetLabel, 'Pause widget');
  assert.equal(editor.advanced.lunchWidgetLabel, 'Déjeuner widget');

  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="week"]').tap();
  await page.evaluate(() => { renderWeek(); window.refreshWeekAppearance658(); });
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const week = await page.evaluate(() => {
    const grid = document.getElementById('weekGrid');
    return {
      lunchLabels: [...grid.querySelectorAll('.week658LunchLabel')].map(node => node.textContent.trim()),
      gapLabels: [...grid.querySelectorAll('.week662GapLabel')].map(node => node.textContent.trim()),
      topEdges: grid.querySelectorAll('.week658LunchRowTop').length,
      bottomEdges: grid.querySelectorAll('.week658LunchRowBottom').length,
      timeCells: grid.querySelectorAll('.wh.timecol.week658LunchTime').length,
    };
  });
  assert.ok(week.lunchLabels.length > 0);
  assert.ok(week.lunchLabels.every(text => text === 'Repas'));
  assert.ok(week.gapLabels.length > 0);
  assert.ok(week.gapLabels.every(text => text === 'Récréation'));
  assert.ok(week.topEdges >= 6);
  assert.ok(week.bottomEdges >= 6);
  assert.ok(week.timeCells > 0);

  await page.locator('.nav[data-mode="today"]').tap();
  await page.waitForFunction(() => document.getElementById('viewToday').classList.contains('active'));
  const hidden = await page.evaluate(() => [...document.querySelectorAll('#viewWeek .week658LunchLabel')].map(node => ({ display: getComputedStyle(node).display, visibility: getComputedStyle(node).visibility })));
  assert.ok(hidden.length > 0);
  assert.ok(hidden.every(item => item.display === 'none' && item.visibility === 'hidden'));
  await page.screenshot({ path: path.join(output, 'today-no-leaked-midi-662.png') });
  assert.deepEqual(errors, []);
  console.log('feedback_662_custom_break_labels=passed');
  console.log('feedback_662_continuous_lunch_lines=passed');
  console.log('feedback_662_today_view_isolated=passed');
  console.log('feedback_662_break_editor_centered=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
