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
  await page.waitForFunction(() => window.__feedback665 && window.prepareSettingsOpen665);

  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => {
    const make = label => ({ enabled: true, courses: [
      { label: label + ' MATIN', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: label + ' SUITE', room: '102', start: '10:00', end: '11:00', slot: 3 },
    ] });
    weeks.A[2] = make('A'); weeks.B[2] = make('B'); breaks.gapLabel = 'Trou'; activeWeek = 'A';
    renderWeek();
    window.__frames665 = [];
    let frame = 0;
    const sample = () => {
      const raw = [...document.querySelectorAll('#weekGrid .gapCell>.cellLabel')]
        .filter(node => getComputedStyle(node).display !== 'none' && getComputedStyle(node).visibility !== 'hidden')
        .map(node => node.textContent.trim());
      window.__frames665.push({ week: document.getElementById('weekTitleLetter').textContent.trim(), raw, text: document.getElementById('weekGrid').innerText });
      if (++frame < 9) requestAnimationFrame(sample);
    };
    requestAnimationFrame(sample);
  });
  await page.locator('#weekTabs .weekTab[data-week="A"]').tap();
  await page.locator('#weekTabs .weekTab[data-week="B"]').tap();
  await page.waitForFunction(() => activeWeek === 'B');
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));
  const frames = await page.evaluate(() => window.__frames665);
  assert.ok(frames.length >= 3);
  assert.ok(frames.every(frame => frame.raw.length === 0), JSON.stringify(frames));
  assert.ok(frames.every(frame => !/(^|\s)Trou(\s|$)/.test(frame.text)), JSON.stringify(frames));

  await page.evaluate(() => {
    const modal = document.getElementById('settingsModal'); window.__settingsFrames665 = [];
    const snapshot = source => {
      if (modal.getAttribute('data-edt-open') !== 'true' && !modal.classList.contains('show')) return;
      window.__settingsFrames665.push({
        source,
        slider: !!document.getElementById('advDensitySlider665'),
        densityInsideText: !!document.getElementById('widgetDensity664')?.closest('#textSettings86'),
        previewHidden: getComputedStyle(document.querySelector('#settingsSheet>.previewGrid')).display === 'none',
      });
    };
    new MutationObserver(() => snapshot('mutation')).observe(modal, { attributes: true, attributeFilter: ['class', 'data-edt-open'] });
    let count = 0; const sample = () => { snapshot('frame'); if (++count < 6) requestAnimationFrame(sample); }; requestAnimationFrame(sample);
  });
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  const settingsFrames = await page.evaluate(() => window.__settingsFrames665);
  assert.ok(settingsFrames.length > 0);
  assert.ok(settingsFrames.every(frame => frame.slider && frame.densityInsideText && frame.previewHidden), JSON.stringify(settingsFrames));

  const sliderReport = await page.evaluate(() => {
    const slider = document.getElementById('advDensitySlider665'), select = document.getElementById('advDensity');
    const report = { type: slider.type, min: slider.min, max: slider.max, step: slider.step, selectDisplay: getComputedStyle(select).display };
    slider.value = '0'; slider.dispatchEvent(new Event('input', { bubbles: true })); slider.dispatchEvent(new Event('change', { bubbles: true }));
    report.compact = JSON.parse(AndroidSchedule.loadAdvancedSettings()).density;
    slider.value = '100'; slider.dispatchEvent(new Event('input', { bubbles: true })); slider.dispatchEvent(new Event('change', { bubbles: true }));
    report.comfortable = JSON.parse(AndroidSchedule.loadAdvancedSettings()).density;
    report.label = document.getElementById('advDensityValue665').textContent.trim();
    return report;
  });
  assert.deepEqual({ type: sliderReport.type, min: sliderReport.min, max: sliderReport.max, step: sliderReport.step }, { type: 'range', min: '0', max: '100', step: '1' });
  assert.equal(sliderReport.selectDisplay, 'none');
  assert.equal(sliderReport.compact, 'compact');
  assert.equal(sliderReport.comfortable, 'comfortable');
  assert.equal(sliderReport.label, '100 %');

  const previewXml = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/res/layout/widget_preview_condensed.xml'), 'utf8');
  assert.ok(!/<View\s/.test(previewXml));
  assert.ok(/5G4 ALL/.test(previewXml) && /ProgressBar/.test(previewXml) && /Midi/.test(previewXml));
  const rowXml = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/res/layout/widget_course_row.xml'), 'utf8');
  const serviceJava = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'), 'utf8');
  assert.ok(/rowCondensedCourseProgress/.test(rowXml));
  assert.ok(/setProgressBar\(R\.id\.rowCondensedCourseProgress/.test(serviceJava));

  await page.screenshot({ path: path.join(output, 'settings-density-665.png'), fullPage: true });
  assert.deepEqual(errors, []);
  console.log('feedback_665_week_gap_first_frames=passed');
  console.log('feedback_665_settings_first_frame=passed');
  console.log('feedback_665_density_slider=passed');
  console.log('feedback_665_condensed_preview=passed');
  console.log('feedback_665_current_course_progress=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
