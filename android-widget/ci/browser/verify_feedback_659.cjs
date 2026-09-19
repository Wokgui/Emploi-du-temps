const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const out = path.resolve(process.env.EDT_BROWSER_OUTPUT || 'smoke-browser');
const asset = path.resolve(__dirname, '../../app/src/main/assets/index.html');

async function verifyWidth(browser, width) {
  const context = await browser.newContext({
    viewport: { width, height: 915 },
    deviceScaleFactor: 1,
    isMobile: true,
    hasTouch: true,
  });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(String(error)));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });
  await page.addInitScript(() => {
    const data = {};
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (typeof key !== 'string') return;
      return (...args) => {
        if (key.startsWith('save')) { data[key.slice(4)] = args[0]; return true; }
        if (key === 'loadSchedule') return data.Schedule || '';
        if (key === 'loadUiSettings') return data.UiSettings || '{"language":"fr","theme":"blue"}';
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
  await page.waitForFunction(() => window.__weekAppearance658 && window.__edtHeavyPanels648 && window.__edtLazyImportReady && document.getElementById('settingsBtn'));
  await page.evaluate(() => window.setModeFromAndroid('edit'));
  await page.waitForTimeout(1200);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  const geometry = await page.evaluate(() => {
    const modal = document.getElementById('settingsModal');
    const sheet = document.getElementById('settingsSheet');
    const week = document.getElementById('week658Settings');
    const body = document.querySelector('#colorSettings86 .settingsSectionBody86');
    const sr = sheet.getBoundingClientRect();
    const wr = week.getBoundingClientRect();
    const rows = [...week.querySelectorAll('.w658Day')].map(row => {
      const r = row.getBoundingClientRect();
      return { left: r.left, right: r.right, width: r.width };
    });
    return {
      directChildren: [...modal.children].map(el => el.id || el.className),
      inSheet: sheet.contains(week),
      inColorSection: !!body && body.contains(week),
      sheet: { left: sr.left, right: sr.right, width: sr.width },
      week: { left: wr.left, right: wr.right, width: wr.width },
      rows,
    };
  });
  assert.deepEqual(geometry.directChildren, ['settingsSheet'], 'Settings must remain one readable sheet');
  assert.equal(geometry.inSheet, true, 'week settings must be inside #settingsSheet');
  assert.equal(geometry.inColorSection, true, 'week colours must join the existing Colors section');
  assert.ok(geometry.week.left >= geometry.sheet.left && geometry.week.right <= geometry.sheet.right, 'week settings overflow the sheet');
  for (const row of geometry.rows) {
    assert.ok(row.left >= geometry.sheet.left && row.right <= geometry.sheet.right, 'a per-day setting row overflows the sheet');
  }
  assert.deepEqual(errors, [], 'JavaScript errors');
  await page.screenshot({ path: path.join(out, `settings-659-${width}.png`) });
  await context.close();
  return geometry;
}

(async () => {
  const browser = await chromium.launch({ headless: true, channel: process.env.EDT_BROWSER_CHANNEL || 'msedge' });
  try {
    for (const width of [412, 360]) {
      const geometry = await verifyWidth(browser, width);
      console.log(`settings_single_sheet_${width}=passed width=${geometry.sheet.width.toFixed(1)}`);
    }
  } finally {
    await browser.close();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
