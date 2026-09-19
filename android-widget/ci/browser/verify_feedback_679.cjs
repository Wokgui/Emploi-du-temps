const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const settle = page => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(() => requestAnimationFrame(resolve)))));

(async () => {
  const browser = await chromium.launch({ headless: true, ...(process.env.EDT_BROWSER_CHANNEL ? { channel: process.env.EDT_BROWSER_CHANNEL } : {}) });
  const context = await browser.newContext({ viewport: { width: 412, height: 915 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(String(error)));
  page.on('console', message => { if (message.type() === 'error') errors.push(message.text()); });

  await page.addInitScript(() => {
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
  await page.waitForFunction(() => window.__feedback678 && typeof window.navigateStable678 === 'function' && typeof window.openBulkCourses === 'function');
  await settle(page);

  const immediate = await page.evaluate(() => {
    const edit = document.querySelector('.nav[data-mode="edit"]');
    edit.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, pointerType: 'touch' }));
    return {
      editActive: edit.classList.contains('active'),
      todayActive: document.querySelector('.nav[data-mode="today"]').classList.contains('active')
    };
  });
  assert.equal(immediate.editActive, true, JSON.stringify(immediate));
  assert.equal(immediate.todayActive, false, JSON.stringify(immediate));
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  await settle(page);

  const editLayout = await page.evaluate(() => {
    const view = document.getElementById('viewEdit');
    const history = document.getElementById('editHistoryActions86');
    const imp = document.getElementById('importPhoto');
    const add = document.getElementById('addCourse');
    const bulk = document.getElementById('addBulkCourses');
    const children = [...view.children];
    const viewRect = view.getBoundingClientRect();
    const metrics = [imp, add, bulk].map(el => {
      const r = el.getBoundingClientRect();
      return { id: el.id, width: r.width, centerDelta: Math.abs((r.left + r.right) / 2 - (viewRect.left + viewRect.right) / 2) };
    });
    return {
      historyBeforeImport: children.indexOf(history) >= 0 && children.indexOf(history) < children.indexOf(imp),
      metrics,
      viewWidth: viewRect.width,
      slotRows: document.querySelectorAll('#slotSettings .slotRow').length,
      editPaddingBottom: parseFloat(getComputedStyle(view).paddingBottom) || 0
    };
  });
  assert.equal(editLayout.historyBeforeImport, true, JSON.stringify(editLayout));
  assert.equal(editLayout.slotRows, 9, JSON.stringify(editLayout));
  assert.ok(editLayout.editPaddingBottom >= 80, JSON.stringify(editLayout));
  editLayout.metrics.forEach(item => {
    assert.ok(item.width < editLayout.viewWidth * .9, JSON.stringify(editLayout));
    assert.ok(item.centerDelta < 4, JSON.stringify(editLayout));
  });

  const ninthVisible = await page.evaluate(async () => {
    const row = document.querySelector('#slotSettings .slotRow:last-child');
    row.scrollIntoView({ block: 'center' });
    await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
    const rr = row.getBoundingClientRect(), nr = document.querySelector('.bottom').getBoundingClientRect();
    return { bottom: rr.bottom, navTop: nr.top, height: rr.height, text: row.textContent };
  });
  assert.ok(ninthVisible.height > 20, JSON.stringify(ninthVisible));
  assert.match(ninthVisible.text, /9/);
  assert.ok(ninthVisible.bottom <= ninthVisible.navTop + 2, JSON.stringify(ninthVisible));

  await page.evaluate(() => { activeWeek = 'A'; currentWeek = 'A'; state = weeks.A; selected = 4; renderEdit(); });
  await page.locator('#addCourse').tap();
  await page.waitForFunction(() => document.getElementById('modal').classList.contains('show'));
  const scopeInitial = await page.evaluate(() => ({
    hidden: document.getElementById('courseWeekField').hidden,
    choices: [...document.querySelectorAll('#courseWeekChoices .courseWeekChoice')].map(b => ({ text: b.textContent, active: b.classList.contains('active') }))
  }));
  assert.equal(scopeInitial.hidden, false, JSON.stringify(scopeInitial));
  assert.deepEqual(scopeInitial.choices.map(x => x.text), ['A', 'B']);
  assert.equal(scopeInitial.choices.find(x => x.text === 'A').active, true);

  await page.locator('#courseWeekChoices .courseWeekChoice', { hasText: 'B' }).tap();
  await page.locator('#fLabel').fill('Cours multi-semaines');
  await page.locator('#fRoom').fill('217');
  await page.locator('#courseForm button[type="submit"]').tap();
  await settle(page);

  const saved = await page.evaluate(() => {
    const a = weeks.A[4].courses.find(c => c.label === 'Cours multi-semaines');
    const b = weeks.B[4].courses.find(c => c.label === 'Cours multi-semaines');
    return {
      inA: !!a, inB: !!b,
      sameSeries: !!a && !!b && a._series === b._series,
      weeksA: a?.weeks || [], weeksB: b?.weeks || []
    };
  });
  assert.equal(saved.inA, true, JSON.stringify(saved));
  assert.equal(saved.inB, true, JSON.stringify(saved));
  assert.equal(saved.sameSeries, true, JSON.stringify(saved));
  assert.deepEqual(saved.weeksA, ['A', 'B']);
  assert.deepEqual(saved.weeksB, ['A', 'B']);

  await page.evaluate(() => {
    activeWeek = 'B'; state = weeks.B; selected = 4; renderEdit();
    const i = weeks.B[4].courses.findIndex(c => c.label === 'Cours multi-semaines');
    openEditor(i);
  });
  await page.waitForFunction(() => document.getElementById('modal').classList.contains('show'));
  const editScope = await page.evaluate(() => [...document.querySelectorAll('#courseWeekChoices .courseWeekChoice')].map(b => ({ week: b.textContent, active: b.classList.contains('active') })));
  assert.equal(editScope.find(x => x.week === 'A').active, true, JSON.stringify(editScope));
  assert.equal(editScope.find(x => x.week === 'B').active, true, JSON.stringify(editScope));
  await page.locator('#cancelEdit').tap();

  await page.evaluate(() => window.openBulkCourses());
  await page.waitForFunction(() => document.getElementById('bulkModalFixed').classList.contains('show'));
  const bulkScope = await page.evaluate(() => ({
    hidden: document.getElementById('bulkWeekFieldFixed').hidden,
    choices: [...document.querySelectorAll('#bulkWeekChoicesFixed .bulkWeekChoice')].map(b => ({ week: b.textContent, active: b.classList.contains('active') }))
  }));
  assert.equal(bulkScope.hidden, false, JSON.stringify(bulkScope));
  assert.deepEqual(bulkScope.choices.map(x => x.week), ['A', 'B']);
  await page.locator('#bulkCancelFixed').tap();

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(() => document.getElementById('settingsModal').getAttribute('data-edt-open') === 'true' || document.getElementById('settingsModal').classList.contains('show'));
  const centered2 = await page.evaluate(() => {
    const box = document.querySelector('.weekCurrentChoices678'), buttons = [...box.querySelectorAll('.weekCurrentChoice678')].filter(b => getComputedStyle(b).display !== 'none');
    const br = box.getBoundingClientRect(), first = buttons[0].getBoundingClientRect(), last = buttons.at(-1).getBoundingClientRect();
    return { count: buttons.length, delta: Math.abs((first.left + last.right) / 2 - (br.left + br.right) / 2) };
  });
  assert.equal(centered2.count, 2, JSON.stringify(centered2));
  assert.ok(centered2.delta < 4, JSON.stringify(centered2));

  await page.locator('.weekCycleChoice678[data-count="3"]').tap();
  await settle(page);
  const centered3 = await page.evaluate(() => {
    const box = document.querySelector('.weekCurrentChoices678'), buttons = [...box.querySelectorAll('.weekCurrentChoice678')].filter(b => getComputedStyle(b).display !== 'none');
    const br = box.getBoundingClientRect(), first = buttons[0].getBoundingClientRect(), last = buttons.at(-1).getBoundingClientRect();
    return { count: buttons.length, delta: Math.abs((first.left + last.right) / 2 - (br.left + br.right) / 2) };
  });
  assert.equal(centered3.count, 3, JSON.stringify(centered3));
  assert.ok(centered3.delta < 4, JSON.stringify(centered3));
  await page.locator('.weekCycleChoice678[data-count="2"]').tap();
  await page.locator('#settingsDone').tap();

  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await settle(page);
  const weekUi = await page.evaluate(() => {
    const top = document.querySelector('#viewWeek .weekTop');
    const cells = [...document.querySelectorAll('#weekGrid .wc')].filter(cell => /Midi|Lunch|Mittag/i.test(cell.textContent || '') && cell.getBoundingClientRect().height > 0);
    return {
      titleDisplay: getComputedStyle(top).display,
      scrollerTopGap: Math.abs(document.querySelector('#viewWeek .weekScroller').getBoundingClientRect().top - document.getElementById('viewWeek').getBoundingClientRect().top),
      lunchCount: cells.length,
      lunchColours: [...new Set(cells.map(cell => getComputedStyle(cell).backgroundColor))]
    };
  });
  assert.equal(weekUi.titleDisplay, 'none', JSON.stringify(weekUi));
  assert.ok(weekUi.scrollerTopGap < 8, JSON.stringify(weekUi));
  assert.ok(weekUi.lunchCount >= 5, JSON.stringify(weekUi));
  assert.equal(weekUi.lunchColours.length, 1, JSON.stringify(weekUi));

  const gradle = fs.readFileSync(path.join(root, 'app/build.gradle'), 'utf8');
  assert.match(gradle, /versionCode 679001/);
  assert.match(gradle, /versionName '6\.79'/);
  assert.deepEqual(errors, [], errors.join('\n'));

  console.log('feedback_679_nav_feedback_is_immediate=passed');
  console.log('feedback_679_edit_actions_order_and_size=passed');
  console.log('feedback_679_ninth_period_reachable=passed');
  console.log('feedback_679_course_week_scope_persists=passed');
  console.log('feedback_679_bulk_week_scope_present=passed');
  console.log('feedback_679_current_week_buttons_centered=passed');
  console.log('feedback_679_week_title_removed_and_lunch_uniform=passed');

  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
