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
  await page.waitForFunction(() => window.__feedback661 && window.__weekAppearance658 && window.__edtHeavyPanels648);

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  const settings = await page.evaluate(() => {
    const legacy = document.getElementById('breakDisplaySetting');
    const colours = document.getElementById('fineSpecialColors');
    const current = document.getElementById('week658Settings');
    return {
      legacyDisplay: legacy ? getComputedStyle(legacy).display : 'missing',
      coloursDisplay: colours ? getComputedStyle(colours).display : 'missing',
      currentVisible: !!current && getComputedStyle(current).display !== 'none',
      visibleText: document.getElementById('settingsSheet').innerText,
    };
  });
  assert.equal(settings.legacyDisplay, 'none');
  assert.equal(settings.coloursDisplay, 'none');
  assert.equal(settings.currentVisible, true);
  assert.doesNotMatch(settings.visibleText, /Midi et trous/);
  await page.screenshot({ path: path.join(output, 'settings-661-412.png') });

  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => { renderWeek(); window.refreshWeekAppearance658(); });
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const lunch = await page.evaluate(() => {
    const heads = [...document.querySelectorAll('#weekGrid > .wh.day')];
    const times = [...document.querySelectorAll('#weekGrid > .wh.timecol')];
    for (const time of times) {
      const found = (time.textContent || '').match(/[0-2]?[0-9]:[0-5][0-9]/g) || [];
      if (found[0] !== '12:00' || found[1] !== '13:00') continue;
      const cells = []; let node = time.nextElementSibling;
      while (node && cells.length < heads.length) { if (node.classList?.contains('wc')) cells.push(node); node = node.nextElementSibling; }
      const row = [time, ...cells];
      return {
        topClasses: row.map(element => element.classList.contains('week658LunchRowTop')),
        bottomClasses: row.map(element => element.classList.contains('week658LunchRowBottom')),
        fontSizes: [...document.querySelectorAll('#weekGrid .week658LunchLabel')].map(element => getComputedStyle(element).fontSize),
        lineLayers: document.getElementById('weekGrid').style.getPropertyValue('--week662-lunch-lines'),
        lineBackground: getComputedStyle(document.getElementById('weekGrid'), '::before').backgroundImage,
      };
    }
    return null;
  });
  assert.ok(lunch, '12-13 lunch row missing');
  assert.ok(lunch.topClasses.every(Boolean), 'top line must cross the complete row');
  assert.ok(lunch.bottomClasses.every(Boolean), 'bottom line must cross the complete row');
  assert.match(lunch.lineLayers, /100% 2px/, 'one uninterrupted two-pixel overlay must paint the lunch boundary');
  assert.notEqual(lunch.lineBackground, 'none', 'the continuous lunch boundary overlay must be visible');
  assert.deepEqual([...new Set(lunch.fontSizes)], ['13px']);
  await page.screenshot({ path: path.join(output, 'week-661-412.png') });
  assert.deepEqual(errors, []);
  console.log('feedback_661_legacy_lunch_settings_removed=passed');
  console.log('feedback_661_lunch_boundary_and_font=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
