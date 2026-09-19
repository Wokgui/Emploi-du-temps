const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { pathToFileURL } = require('node:url');

const root = path.resolve(__dirname, '../..');
const chunks = path.resolve(process.env.EDT_UI_CHUNKS || 'smoke-browser/chunks');
const asset = path.resolve(root, 'app/src/main/assets/index.html');
const settle = page => page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));

(async () => {
  const browser = await chromium.launch({ headless: true, ...(process.env.EDT_BROWSER_CHANNEL ? { channel: process.env.EDT_BROWSER_CHANNEL } : {}) });
  const context = await browser.newContext({ viewport: { width: 412, height: 915 }, isMobile: true, hasTouch: true });
  const page = await context.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push(String(e)));
  page.on('console', m => { if (m.type() === 'error') errors.push(m.text()); });

  await page.addInitScript(() => {
    const initialSlots = [
      ['08:00','09:00'],['09:00','10:00'],['10:00','11:00'],['11:00','12:00'],
      ['13:00','14:00'],['14:00','15:00'],['16:00','17:00'],['17:00','18:00'],['18:00','19:00']
    ].map(x => ({start:x[0], end:x[1]}));
    const day = courses => ({enabled:true,courses});
    const monday = [
      {start:'08:00',end:'09:00',label:'A',room:'1',slot:1},
      {start:'10:00',end:'11:00',label:'B',room:'1',slot:3},
      {start:'11:00',end:'12:00',label:'C',room:'1',slot:4},
      {start:'13:00',end:'14:00',label:'D',room:'1',slot:5}
    ];
    const makeWeek = () => ({
      '2': day(monday.map(x=>({...x}))),
      '3': day([]),'4': day([]),'5': day([]),'6': day([]),'7': day([]),'1': day([])
    });
    let schedule = JSON.stringify({
      _slots: initialSlots, _slotConfigV2:true, _currentWeek:'A',
      _breaks:{gapLabel:'Trou',lunchLabel:'Midi',showGapBadge:true,showLunchBadge:true},
      _weeks:{A:makeWeek(),B:makeWeek(),C:makeWeek(),D:makeWeek()}
    });
    const data = {
      AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"density":"normal","widgetAutoDensity":true}',
      UiSettings:'{"language":"fr","theme":"blue"}'
    };
    window.confirm = () => true;
    window.alert = () => {};
    window.AndroidSchedule = new Proxy({}, { get(target, key) {
      if (key === 'loadSchedule') return () => schedule;
      if (key === 'saveSchedule') return raw => { schedule = raw; window.__savedSchedule684 = raw; return true; };
      if (key === 'loadAdvancedSettings') return () => data.AdvancedSettings;
      if (key === 'saveAdvancedSettings') return raw => { data.AdvancedSettings = raw; return true; };
      if (key === 'loadUiSettings') return () => data.UiSettings;
      if (key === 'saveUiSettings') return raw => { data.UiSettings = raw; return true; };
      if (key === 'listProfiles') return () => '{"current":"main","profiles":[{"id":"main","name":"Profil principal"}]}';
      return (...args) => {
        if (String(key).startsWith('load')) return '{}';
        if (String(key).startsWith('list') || String(key).startsWith('supported')) return '[]';
        return true;
      };
    }});
  });

  await page.goto(pathToFileURL(asset).href);
  for (const file of fs.readdirSync(chunks).sort()) {
    await page.evaluate(fs.readFileSync(path.join(chunks, file), 'utf8') + '\n//# sourceURL=' + file);
    await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(() => window.__feedback680 && window.__edtHeavyPanels648);
  await settle(page);

  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(() => document.getElementById('viewEdit').classList.contains('active'));
  await settle(page);

  let state = await page.evaluate(() => ({
    rows: document.querySelectorAll('#slotSettings .slotRow').length,
    buttons: document.querySelectorAll('#slotSettings .slotRemove').length,
    slots: slots.length
  }));
  assert.deepEqual(state, {rows:9,buttons:9,slots:9}, JSON.stringify(state));

  await page.locator('#slotSettings .slotRemove').nth(8).tap();
  await page.waitForFunction(() => document.querySelectorAll('#slotSettings .slotRow').length === 8);
  state = await page.evaluate(() => ({
    rows: document.querySelectorAll('#slotSettings .slotRow').length,
    slots: slots.length,
    saved: JSON.parse(window.__savedSchedule684 || '{}')._slots?.length
  }));
  assert.deepEqual(state, {rows:8,slots:8,saved:8}, JSON.stringify(state));

  await page.evaluate(() => window.reloadSchedule());
  await settle(page);
  state = await page.evaluate(() => ({
    rows: document.querySelectorAll('#slotSettings .slotRow').length,
    slots: slots.length
  }));
  assert.deepEqual(state, {rows:8,slots:8}, JSON.stringify(state));

  // First synchronous Week paint must already have a single lunch colour.
  await page.evaluate(() => window.setModeFromAndroid('week'));
  const lunch = await page.evaluate(() => {
    const cells = [...document.querySelectorAll('#weekGrid .wc')].filter(c =>
      c.classList.contains('week658Lunch') || c.classList.contains('lunchCell') ||
      c.classList.contains('dynamicLunchCell') || c.classList.contains('nativeLunchCell') ||
      c.classList.contains('finalLunchCell') || c.classList.contains('lunch655Synthetic'));
    const visible = cells.filter(c => c.getBoundingClientRect().height > 0);
    return {
      count: visible.length,
      colours: [...new Set(visible.map(c => getComputedStyle(c).backgroundColor))],
      shadows: [...new Set(visible.map(c => getComputedStyle(c).boxShadow))]
    };
  });
  assert.ok(lunch.count >= 5, JSON.stringify(lunch));
  assert.equal(lunch.colours.length, 1, JSON.stringify(lunch));
  assert.deepEqual(errors, [], errors.join('\n'));

  const java = fs.readFileSync(path.join(root,'app/src/main/java/com/wokgui/schedulewidget/ScheduleStore.java'),'utf8');
  assert.match(java, /SLOT_COUNT/);
  assert.match(java, /Math\.max\(1, Math\.min\(10, slots\.length\(\)\)\)/);
  assert.match(java, /for \(int i = 1; i <= slotCount; i\+\+\)/);

  console.log('feedback_684_touch_remove_hour_persists=passed');
  console.log('feedback_684_first_week_lunch_frame_uniform=passed');
  await context.close();
  await browser.close();
})().catch(error => { console.error(error); process.exitCode = 1; });
