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
        if (key === 'listProfiles') return '{"current":"main","profiles":[{"id":"main","name":"Profil principal"},{"id":"second","name":"Profil 2"}]}';
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
  await page.waitForFunction(() => window.__feedback664 && window.__edtNavigationCacheV2);

  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => {
    weeks.A[2].courses[0].label = 'A ATOMIQUE';
    weeks.B[2].courses[0].label = 'B ATOMIQUE';
    activeWeek = 'A';
    renderContext();
    renderWeek();
    window.__weekFrames664 = [];
    let count = 0;
    const sample = () => {
      window.__weekFrames664.push({
        week: document.getElementById('weekTitleLetter')?.textContent.trim(),
        label: document.querySelector('#weekGrid .wc.has .cellLabel')?.textContent.trim(),
        cells: document.querySelectorAll('#weekGrid .wc').length,
      });
      if (++count < 7) requestAnimationFrame(sample);
    };
    requestAnimationFrame(sample);
  });
  await page.locator('#weekTabs .weekTab[data-week="B"]').tap();
  await page.waitForFunction(() => activeWeek === 'B');
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const switchReport = await page.evaluate(() => ({
    activeWeek,
    title: document.getElementById('weekTitleLetter').textContent.trim(),
    firstLabel: document.querySelector('#weekGrid .wc.has .cellLabel').textContent.trim(),
    frames: window.__weekFrames664,
  }));
  assert.equal(switchReport.activeWeek, 'B');
  assert.equal(switchReport.title, 'B');
  assert.equal(switchReport.firstLabel, 'B ATOMIQUE');
  assert.ok(switchReport.frames.length >= 2);
  assert.ok(switchReport.frames.every(frame => frame.cells > 0));
  assert.ok(switchReport.frames.every(frame =>
    (frame.week === 'A' && frame.label === 'A ATOMIQUE') ||
    (frame.week === 'B' && frame.label === 'B ATOMIQUE')
  ), JSON.stringify(switchReport.frames));

  await page.locator('.nav[data-mode="today"]').tap();
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  const currentSelection = await page.evaluate(() => {
    const js = new Date().getDay();
    const today = js >= 1 && js <= 5 ? js + 1 : null;
    const expected = today && weeks[currentWeek][today].courses.length
      ? today
      : DAYS.find(day => weeks[currentWeek][day].courses.length);
    return { selected, expected, currentWeek, activeWeek };
  });
  assert.equal(currentSelection.selected, currentSelection.expected);
  assert.equal(currentSelection.activeWeek, currentSelection.currentWeek);

  const fallback = await page.evaluate(() => {
    const js = new Date().getDay();
    const today = js >= 1 && js <= 5 ? js + 1 : null;
    if (today) weeks[currentWeek][today].courses = [];
    const start = today && DAYS.includes(today) ? DAYS.indexOf(today) : 0;
    const ordered = DAYS.slice(start).concat(DAYS.slice(0, start));
    const expected = ordered.find(day => weeks[currentWeek][day].courses.length) || today || DAYS[0];
    selected = DAYS.find(day => day !== expected) || DAYS[0];
    return { expected };
  });
  await page.locator('.nav[data-mode="today"]').tap();
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(expected => selected === expected, fallback.expected);
  assert.equal(await page.evaluate(() => selected), fallback.expected);

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  await page.waitForFunction(() => document.getElementById('widgetDensity664'));
  await page.evaluate(() => { document.getElementById('advancedSettings85').open = true; });
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const layout = await page.evaluate(() => {
    const preview = document.querySelector('#settingsSheet>.previewGrid');
    const density = document.getElementById('widgetDensity664');
    const profile = document.getElementById('advProfileSelect');
    const title = document.getElementById('advProfilesTitle');
    return {
      previewDisplay: getComputedStyle(preview).display,
      previewHidden: preview.getAttribute('aria-hidden'),
      densityInsideText: density.closest('#textSettings86') !== null,
      densityInsideAdvanced: density.closest('#advancedSettings85') !== null,
      densityLabel: density.querySelector('.feedback664DensityTitle').textContent.trim(),
      profileWidth: profile.getBoundingClientRect().width,
      profileParentWidth: profile.parentElement.getBoundingClientRect().width,
      profileTextAlign: getComputedStyle(profile).textAlign,
      profileTitleAlign: getComputedStyle(title).textAlign,
      profileTitle: title.textContent.trim(),
    };
  });
  assert.equal(layout.previewDisplay, 'none');
  assert.equal(layout.previewHidden, 'true');
  assert.equal(layout.densityInsideText, true);
  assert.equal(layout.densityInsideAdvanced, false);
  assert.equal(layout.densityLabel, 'Condensation du widget');
  assert.ok(layout.profileWidth > 140 && layout.profileWidth <= 260, JSON.stringify(layout));
  assert.ok(layout.profileWidth < layout.profileParentWidth);
  assert.equal(layout.profileTextAlign, 'center');
  assert.equal(layout.profileTitleAlign, 'center');
  assert.equal(layout.profileTitle, 'Profils');

  await page.locator('#advDensity').selectOption('compact');
  let advanced = await page.evaluate(() => JSON.parse(AndroidSchedule.loadAdvancedSettings() || '{}'));
  assert.equal(advanced.density, 'compact');
  await page.locator('#advDensity').selectOption('normal');
  advanced = await page.evaluate(() => JSON.parse(AndroidSchedule.loadAdvancedSettings() || '{}'));
  assert.equal(advanced.density, 'normal');

  await page.screenshot({ path: path.join(output, 'settings-polish-664.png'), fullPage: true });
  assert.deepEqual(errors, []);
  console.log('feedback_664_edit_day_and_fallback=passed');
  console.log('feedback_664_atomic_week_switch=passed');
  console.log('feedback_664_settings_preview_removed=passed');
  console.log('feedback_664_density_in_text_section=passed');
  console.log('feedback_664_profiles_centered=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
