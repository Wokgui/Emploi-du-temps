const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const root = path.resolve(__dirname, '../..');
const asset = path.resolve(root, 'app/src/main/assets/index.html');

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
  await page.waitForFunction(() => window.__feedback673 && typeof window.beginEditSwap673 === 'function' && typeof window.finishEditSwap673 === 'function');
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  await page.evaluate(() => {
    const makeDay = (prefix, suffix) => ({ enabled: true, courses: [
      { label: prefix + '-08', room: '101', start: '08:00', end: '09:00', slot: 1 },
      { label: prefix + '-10', room: '102', start: '10:00', end: '11:00', slot: 3 },
      { label: prefix + '-' + suffix, room: '201', start: suffix + ':00', end: String(Number(suffix) + 1).padStart(2, '0') + ':00', slot: 6 }
    ] });
    weeks.A[2] = makeDay('EDIT-A', '13');
    weeks.B[2] = makeDay('EDIT-B', '15');
    activeWeek = 'A';
    selected = 2;
    renderEdit();
  });
  await page.waitForFunction(() => document.getElementById('editList').innerText.includes('EDIT-A'));
  await page.evaluate(() => new Promise(resolve => setTimeout(() => requestAnimationFrame(() => requestAnimationFrame(resolve)), 0)));

  for (let i = 0; i < 60; i++) {
    const target = i % 2 ? 'A' : 'B';
    const previous = target === 'A' ? 'EDIT-B' : 'EDIT-A';
    const expected = target === 'A' ? 'EDIT-A' : 'EDIT-B';
    const states = await page.evaluate(({ target }) => new Promise(resolve => {
      const take = () => {
        const views = [...document.querySelectorAll('#viewEdit')];
        const live = views.find(node => !node.classList.contains('editSwapCover673')) || null;
        const cover = document.querySelector('.editSwapCover673');
        const style = cover ? getComputedStyle(cover) : null;
        return {
          views: views.length,
          covers: document.querySelectorAll('.editSwapCover673').length,
          liveText: live ? live.innerText : '',
          coverText: cover ? cover.innerText : '',
          coverId: cover ? cover.id : '',
          liveRows: live ? live.querySelectorAll('#editList .editCourse').length : 0,
          coverRows: cover ? cover.querySelectorAll('#editList .editCourse').length : 0,
          liveDayTabs: live ? live.querySelectorAll('#dayTabs .dayTab').length : 0,
          coverDayTabs: cover ? cover.querySelectorAll('#dayTabs .dayTab').length : 0,
          coverOpacity: style ? style.opacity : '',
          coverZ: style ? Number(style.zIndex) : 0,
          coverWidth: cover ? cover.getBoundingClientRect().width : 0,
          coverHeight: cover ? cover.getBoundingClientRect().height : 0,
          title: live && live.querySelector('#editDayTitle') ? live.querySelector('#editDayTitle').textContent.trim() : ''
        };
      };
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
              requestAnimationFrame(() => resolve({ immediate, afterTimers, firstPaint, secondPaint, thirdPaint, finalPaint: take() }));
            });
          });
        });
      }, 0);
    }), { target });

    for (const phase of [states.immediate, states.afterTimers]) {
      assert.equal(phase.covers, 0, JSON.stringify(states));
      assert.equal(phase.views, 1, JSON.stringify(states));
      assert.ok(phase.liveText.includes(previous) && !phase.liveText.includes(expected), JSON.stringify(states));
    }
    for (const phase of [states.firstPaint, states.secondPaint, states.thirdPaint]) {
      assert.equal(phase.covers, 1, JSON.stringify(states));
      assert.equal(phase.views, 2, JSON.stringify(states));
      assert.equal(phase.coverId, 'viewEdit', JSON.stringify(states));
      assert.equal(phase.coverOpacity, '1', JSON.stringify(states));
      assert.ok(phase.coverZ >= 35, JSON.stringify(states));
      assert.ok(phase.coverWidth > 100 && phase.coverHeight > 100, JSON.stringify(states));
      assert.equal(phase.coverDayTabs, phase.liveDayTabs, JSON.stringify(states));
      assert.ok(phase.coverDayTabs >= 5, JSON.stringify(states));
      assert.equal(phase.coverRows, 3, JSON.stringify(states));
      assert.equal(phase.liveRows, 3, JSON.stringify(states));
      assert.ok(phase.coverText.includes(previous) && !phase.coverText.includes(expected), JSON.stringify(states));
      assert.ok(phase.liveText.includes(expected) && !phase.liveText.includes(previous), JSON.stringify(states));
      assert.ok(phase.title.includes('Semaine ' + target), JSON.stringify(states));
    }
    assert.equal(states.finalPaint.covers, 0, JSON.stringify(states));
    assert.equal(states.finalPaint.views, 1, JSON.stringify(states));
    assert.ok(states.finalPaint.liveText.includes(expected) && !states.finalPaint.liveText.includes(previous), JSON.stringify(states));
  }

  const feedback = fs.readFileSync(path.resolve(root, 'app/src/main/java/com/wokgui/schedulewidget/Feedback673Ui.java'), 'utf8');
  const actionChain = fs.readFileSync(path.resolve(root, 'app/src/main/java/com/wokgui/schedulewidget/ActionChainUi651.java'), 'utf8');
  const chunksJava = fs.readFileSync(path.resolve(root, 'app/src/main/java/com/wokgui/schedulewidget/ChunkedUiScripts.java'), 'utf8');
  const gradle = fs.readFileSync(path.resolve(root, 'app/build.gradle'), 'utf8');
  assert.match(feedback, /copyFormState/);
  assert.match(feedback, /window\.beginEditSwap673=beginSwap/);
  assert.match(feedback, /window\.finishEditSwap673=finishSwap/);
  assert.match(feedback, /setTimeout\(\(\)=>requestAnimationFrame\(\(\)=>requestAnimationFrame/);
  assert.doesNotMatch(feedback, /removeAttribute\('id'\)/);
  assert.match(actionChain, /window\.beginEditSwap673/);
  assert.match(actionChain, /window\.finishEditSwap673\(editCover\)/);
  assert.match(chunksJava, /Feedback673Ui\.script\(\)/);
  assert.match(gradle, /versionCode 673001/);
  assert.match(gradle, /versionName '6\.73'/);
  assert.deepEqual(errors, []);
  console.log('feedback_673_edit_week_snapshot_60_switches=passed');
  console.log('feedback_673_edit_controls_and_rows_atomic=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
