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
  await page.waitForFunction(() => window.__feedback666 && typeof window.finishWeekSwap671 === 'function');
  await page.locator('.nav[data-mode="week"]').tap();
  await page.waitForFunction(() => document.getElementById('viewWeek').classList.contains('active'));
  await page.evaluate(() => {
    const makeDay = (prefix, afternoon) => ({ enabled: true, courses: [
      { label: prefix + '-08', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: prefix + '-10', room: '102', start: '10:00', end: '11:00', slot: 3 },
      { label: prefix + '-11', room: '103', start: '11:00', end: '12:00', slot: 4 },
      { label: prefix + '-' + afternoon, room: '201', start: afternoon + ':00', end: String(Number(afternoon) + 1).padStart(2, '0') + ':00', slot: 6 }
    ] });
    weeks.A[2] = makeDay('TYPE-A', '13');
    weeks.B[2] = makeDay('TYPE-B', '15');
    activeWeek = 'A';
    renderWeek();
  });
  await page.evaluate(() => new Promise(resolve => setTimeout(() => requestAnimationFrame(() => requestAnimationFrame(resolve)), 0)));

  for (let i = 0; i < 60; i++) {
    const target = i % 2 ? 'A' : 'B';
    const previous = target === 'A' ? 'TYPE-B' : 'TYPE-A';
    const expected = target === 'A' ? 'TYPE-A' : 'TYPE-B';
    const states = await page.evaluate(({ target }) => new Promise(resolve => {
      const take = () => {
        const live = document.getElementById('weekGrid');
        const cover = document.querySelector('.weekSwapCover669');
        const gap = cover && cover.querySelector('.gapCell>.cellLabel');
        const coverLunch = cover && cover.querySelector('.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell');
        const lunchStyle = coverLunch ? getComputedStyle(coverLunch) : null;
        return {
          grids: document.querySelectorAll('.weekGrid').length,
          covers: document.querySelectorAll('.weekSwapCover669').length,
          liveText: live ? live.innerText : '',
          coverText: cover ? cover.innerText : '',
          coverId: cover ? cover.id : '',
          coverGapDisplay: gap ? getComputedStyle(gap).display : 'missing',
          coverOpacity: cover ? getComputedStyle(cover).opacity : '',
          coverZ: cover ? Number(getComputedStyle(cover).zIndex) : 0,
          coverWidth: cover ? cover.getBoundingClientRect().width : 0,
          coverHeight: cover ? cover.getBoundingClientRect().height : 0,
          coverLunchCount: cover ? cover.querySelectorAll('.lunchCell,.dynamicLunchCell,.nativeLunchCell,.finalLunchCell').length : 0,
          coverLunchHeight: coverLunch ? coverLunch.getBoundingClientRect().height : 0,
          coverLunchColor: lunchStyle ? lunchStyle.backgroundColor : '',
          coverLunchBorderTop: lunchStyle ? lunchStyle.borderTopWidth + ' ' + lunchStyle.borderTopStyle + ' ' + lunchStyle.borderTopColor : '',
          coverLunchBorderBottom: lunchStyle ? lunchStyle.borderBottomWidth + ' ' + lunchStyle.borderBottomStyle + ' ' + lunchStyle.borderBottomColor : '',
          title: document.getElementById('weekTitleLetter').textContent.trim()
        };
      };
      const oldLunch = document.querySelector('#weekGrid .lunchCell,#weekGrid .dynamicLunchCell,#weekGrid .nativeLunchCell,#weekGrid .finalLunchCell');
      const oldLunchStyle = oldLunch ? getComputedStyle(oldLunch) : null;
      const beforeLunch = oldLunchStyle ? {
        height: oldLunch.getBoundingClientRect().height,
        color: oldLunchStyle.backgroundColor,
        borderTop: oldLunchStyle.borderTopWidth + ' ' + oldLunchStyle.borderTopStyle + ' ' + oldLunchStyle.borderTopColor,
        borderBottom: oldLunchStyle.borderBottomWidth + ' ' + oldLunchStyle.borderBottomStyle + ' ' + oldLunchStyle.borderBottomColor
      } : null;
      document.querySelector('#weekTabs .weekTab[data-week="' + target + '"]').click();
      const immediate = take();
      setTimeout(() => {
        const afterTimers = take();
        requestAnimationFrame(() => {
          const firstPaint = take();
          requestAnimationFrame(() => {
            const secondPaint = take();
            requestAnimationFrame(() => {
              const thirdPaint = take();
              requestAnimationFrame(() => {
                const finalPaint = take();
                resolve({ beforeLunch, immediate, afterTimers, firstPaint, secondPaint, thirdPaint, finalPaint });
              });
            });
          });
        });
      }, 0);
    }), { target });

    for (const phase of [states.immediate, states.afterTimers]) {
      assert.equal(phase.covers, 0, JSON.stringify(states));
      assert.equal(phase.grids, 1, JSON.stringify(states));
      assert.ok(phase.liveText.includes(previous) && !phase.liveText.includes(expected), JSON.stringify(states));
    }
    for (const phase of [states.firstPaint, states.secondPaint, states.thirdPaint]) {
      assert.equal(phase.covers, 1, JSON.stringify(states));
      assert.equal(phase.grids, 2, JSON.stringify(states));
      assert.equal(phase.coverId, 'weekGrid', JSON.stringify(states));
      assert.equal(phase.coverGapDisplay, 'none', JSON.stringify(states));
      assert.equal(phase.coverOpacity, '1', JSON.stringify(states));
      assert.ok(phase.coverZ >= 30, JSON.stringify(states));
      assert.ok(phase.coverWidth > 100 && phase.coverHeight > 100, JSON.stringify(states));
      assert.ok(states.beforeLunch, JSON.stringify(states));
      assert.ok(phase.coverLunchCount > 0 && phase.coverLunchHeight > 0, JSON.stringify(states));
      assert.equal(phase.coverLunchHeight, states.beforeLunch.height, JSON.stringify(states));
      assert.equal(phase.coverLunchColor, states.beforeLunch.color, JSON.stringify(states));
      assert.equal(phase.coverLunchBorderTop, states.beforeLunch.borderTop, JSON.stringify(states));
      assert.equal(phase.coverLunchBorderBottom, states.beforeLunch.borderBottom, JSON.stringify(states));
      assert.ok(phase.coverText.includes(previous) && !phase.coverText.includes(expected), JSON.stringify(states));
      assert.ok(phase.liveText.includes(expected) && !phase.liveText.includes(previous), JSON.stringify(states));
      assert.equal(phase.title, target, JSON.stringify(states));
    }
    assert.equal(states.finalPaint.covers, 0, JSON.stringify(states));
    assert.equal(states.finalPaint.grids, 1, JSON.stringify(states));
    assert.ok(states.finalPaint.liveText.includes(expected) && !states.finalPaint.liveText.includes(previous), JSON.stringify(states));
  }

  const weekCover = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/Feedback666Ui.java'), 'utf8');
  const actionChain = fs.readFileSync(path.resolve(__dirname, '../../app/src/main/java/com/wokgui/schedulewidget/ActionChainUi651.java'), 'utf8');
  assert.match(weekCover, /window\.finishWeekSwap671=finishSwap/);
  assert.match(weekCover, /setTimeout\(\(\)=>requestAnimationFrame\(\(\)=>requestAnimationFrame/);
  assert.match(weekCover, /weekSwapToken671/);
  assert.doesNotMatch(weekCover, /queueMicrotask/);
  assert.doesNotMatch(weekCover, /cover\.removeAttribute\('id'\)/);
  assert.match(actionChain, /window\.finishWeekSwap671\(cover\)/);
  assert.deepEqual(errors, []);
  console.log('feedback_671_old_week_masks_intermediate_paints=passed');
  console.log('feedback_671_final_week_revealed_after_barrier=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });