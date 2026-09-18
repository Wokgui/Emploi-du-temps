const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
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
        if (key === 'loadAdvancedSettings') return data.AdvancedSettings || '{"density":"normal","widgetDensityPercent":50,"widgetAutoDensity":false,"showBreaksWeek":true,"showLunchWeek":true}';
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
  await page.waitForFunction(() => window.__feedback666 && window.__edtNavigationCacheV2);

  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => {
    const day = label => ({ enabled: true, courses: [
      { label: label + '-MATIN', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: label + '-APRES', room: '102', start: '10:00', end: '11:00', slot: 3 },
    ] });
    weeks.A[2] = day('AAA'); weeks.B[2] = day('BBB'); activeWeek = 'A'; renderWeek();
    window.__weekGridRef667 = document.getElementById('weekGrid');
    window.__weekFrames667 = [];
    window.__sampleWeek667 = () => {
      const grid = document.getElementById('weekGrid');
      const title = document.getElementById('weekTitleLetter').textContent.trim();
      const text = grid.innerText;
      const lunchCells = grid.querySelectorAll('.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell').length;
      window.__weekFrames667.push({
        title, text, lunchCells,
        sameGrid: grid === window.__weekGridRef667,
        children: grid.childElementCount,
        grids: document.querySelectorAll('.weekGrid').length,
        covers: document.querySelectorAll('.weekSwapCover669').length
      });
    };
  });
  for (let i = 0; i < 60; i++) {
    const letter = i % 2 ? 'A' : 'B';
    await page.locator('#weekTabs .weekTab[data-week="' + letter + '"]').tap();
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => { window.__sampleWeek667(); resolve(); })));
  }
  const weekFrames = await page.evaluate(() => window.__weekFrames667);
  assert.equal(weekFrames.length, 60);
  assert.ok(weekFrames.every(frame => frame.grids === 2 && frame.covers === 1 && frame.sameGrid && frame.children > 6), JSON.stringify(weekFrames));
  assert.ok(weekFrames.every(frame => frame.lunchCells > 0), JSON.stringify(weekFrames));
  assert.ok(weekFrames.every(frame => frame.title === 'A' ? frame.text.includes('AAA') && !frame.text.includes('BBB') : frame.text.includes('BBB') && !frame.text.includes('AAA')), JSON.stringify(weekFrames));
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  assert.equal(await page.locator('.weekSwapCover669').count(), 0);
  assert.equal(await page.evaluate(() => window.__feedback666 === true), true);

  await page.locator('.nav[data-mode="today"]').tap();
  await page.evaluate(() => {
    const edit = document.getElementById('viewEdit');
    document.getElementById('editList').innerHTML = '';
    window.invalidateTimetableViews(['edit']);
    window.__editFrames666 = [];
    new MutationObserver(() => {
      if (edit.classList.contains('active')) {
        window.__editFrames666.push({
          rows: document.getElementById('editList').childElementCount,
          tabs: document.getElementById('dayTabs').childElementCount,
          title: document.getElementById('editDayTitle').textContent.trim(),
          stageHeight: parseFloat(document.querySelector('main.wrap').style.height || '0'),
          viewHeight: edit.offsetHeight
        });
      }
    }).observe(edit, { attributes: true, attributeFilter: ['class'] });
  });
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  const editFrames = await page.evaluate(() => window.__editFrames666);
  assert.ok(editFrames.length > 0);
  assert.ok(editFrames.every(frame => frame.rows > 0 && frame.tabs > 0 && frame.title.length > 0 && frame.stageHeight >= frame.viewHeight), JSON.stringify(editFrames));

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => window.__edtHeavyPanels648.isOpen('settings'));
  const settings = await page.evaluate(() => {
    const title = document.getElementById('advCalendarTitle');
    const calendar = title.closest('.settingBox');
    const school = document.getElementById('schoolCalendarSetting');
    const slider = document.getElementById('advDensitySlider665');
    const automatic = document.getElementById('advDensityAuto665');
    slider.value = '37';
    slider.dispatchEvent(new Event('input', { bubbles: true }));
    slider.dispatchEvent(new Event('change', { bubbles: true }));
    automatic.checked = true;
    automatic.dispatchEvent(new Event('change', { bubbles: true }));
    const saved = JSON.parse(AndroidSchedule.loadAdvancedSettings());
    return {
      title: title.textContent.trim(),
      adjacent: school.nextSibling === calendar,
      sameParent: school.parentNode === calendar.parentNode,
      slider: { min: slider.min, max: slider.max, step: slider.step, value: saved.widgetDensityPercent },
      automatic: saved.widgetAutoDensity,
      autoText: document.getElementById('advDensityAutoRow665').innerText.trim()
    };
  });
  assert.equal(settings.title, 'Jours sans cours');
  assert.equal(settings.adjacent, true);
  assert.equal(settings.sameParent, true);
  assert.deepEqual(settings.slider, { min: '0', max: '100', step: '1', value: 37 });
  assert.equal(settings.automatic, true);
  assert.match(settings.autoText, /taille du widget/i);

  const widgetInfo = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/res/xml/widget_info.xml'), 'utf8');
  assert.ok(!/android:configure=/.test(widgetInfo));
  const preview = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/res/layout/widget_preview_condensed.xml'), 'utf8');
  const lunch = preview.slice(preview.indexOf('android:background="#FFE08A"') - 600, preview.indexOf('android:background="#FFE08A"') + 200);
  assert.match(lunch, /android:layout_height="20dp"/);
  assert.ok(!/android:layout_height="0dp"/.test(lunch));
  assert.match(preview, /android:gravity="center_vertical"/);

  const advanced = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/AdvancedSettingsStore.java'), 'utf8');
  const condensed = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/CondensedCoursesService.java'), 'utf8');
  const sizing = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/CondensedRowSizing.java'), 'utf8');
  const otherWidgets = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/UpcomingCoursesService.java'), 'utf8');
  assert.match(advanced, /widgetAutoDensity/);
  assert.match(condensed, /widgetAutoDensity\(context\)/);
  assert.ok((otherWidgets.match(/widgetAutoDensity\(context\)/g) || []).length >= 2);
  assert.match(sizing, /available % count/);
  assert.match(otherWidgets, /autoRowHeightDp\(widgetHeightDp, items\.size\(\), position\)/);

  assert.deepEqual(errors, []);
  console.log('feedback_667_week_lunch_persistent=passed');
  console.log('feedback_667_week_grid_identity_and_visibility=passed');
  console.log('feedback_667_edit_prepaint=passed');
  console.log('feedback_667_widget_full_height=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
