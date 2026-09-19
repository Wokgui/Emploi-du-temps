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
  await page.waitForFunction(() => window.__feedback663 && window.__edtHeavyPanels648);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  await page.waitForFunction(() => document.getElementById('feedback663Visibility'));

  const layout = await page.evaluate(() => {
    const box = document.getElementById('breakDisplaySetting');
    const root = document.getElementById('feedback663Visibility');
    const cells = [...root.querySelectorAll('.feedback663Cell')];
    return {
      boxDisplay: getComputedStyle(box).display,
      title: root.querySelector('.feedback663Title').textContent.trim(),
      checkboxes: root.querySelectorAll('input[type="checkbox"]').length,
      oldDisplay: getComputedStyle(document.getElementById('breakVisibility70')).display,
      centered: cells.every(cell => getComputedStyle(cell).textAlign === 'center'),
      text: root.innerText,
    };
  });
  assert.notEqual(layout.boxDisplay, 'none');
  assert.equal(layout.title, 'Affichage des interruptions');
  assert.equal(layout.checkboxes, 4);
  assert.equal(layout.oldDisplay, 'none');
  assert.equal(layout.centered, true);
  assert.match(layout.text, /Application/);
  assert.match(layout.text, /Widget/);
  assert.match(layout.text, /Midi/);
  assert.match(layout.text, /Trous/);
  assert.doesNotMatch(layout.text, /Aujourd’hui|Semaine/);

  for (const id of ['feedback663AppLunch', 'feedback663AppGaps', 'feedback663WidgetLunch', 'feedback663WidgetGaps']) {
    await page.locator('#' + id).uncheck();
  }
  let advanced = await page.evaluate(() => JSON.parse(AndroidSchedule.loadAdvancedSettings() || '{}'));
  assert.equal(advanced.showLunchToday, false);
  assert.equal(advanced.showLunchWeek, false);
  assert.equal(advanced.showBreaksToday, false);
  assert.equal(advanced.showBreaksWeek, false);
  assert.equal(advanced.showLunch, false);
  assert.equal(advanced.showBreaks, false);

  for (const id of ['feedback663AppLunch', 'feedback663AppGaps', 'feedback663WidgetLunch', 'feedback663WidgetGaps']) {
    await page.locator('#' + id).check();
  }
  advanced = await page.evaluate(() => JSON.parse(AndroidSchedule.loadAdvancedSettings() || '{}'));
  assert.equal(advanced.showLunchToday, true);
  assert.equal(advanced.showLunchWeek, true);
  assert.equal(advanced.showBreaksToday, true);
  assert.equal(advanced.showBreaksWeek, true);
  assert.equal(advanced.showLunch, true);
  assert.equal(advanced.showBreaks, true);

  await page.screenshot({ path: path.join(output, 'settings-break-matrix-663.png') });
  assert.deepEqual(errors, []);
  console.log('feedback_663_centered_visibility_matrix=passed');
  console.log('feedback_663_independent_app_widget_switches=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
