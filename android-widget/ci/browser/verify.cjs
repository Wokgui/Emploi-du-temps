// Full UI integration: real Java-generated code, isolated in-memory Android storage.
// Browser timings are supplementary; Android CI remains the performance release gate.
const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');
const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');
const out=path.resolve(process.env.EDT_BROWSER_OUTPUT||'smoke-browser');
const asset=path.resolve(__dirname,'../../app/src/main/assets/index.html');
fs.mkdirSync(out,{recursive:true});
let browser;
async function openApp(name) {
  const context=await browser.newContext({viewport:{width:412,height:915},deviceScaleFactor:1,isMobile:true,hasTouch:true});
  const page=await context.newPage();
  const logs=[],errors=[];
  page.on('console',m=>{logs.push(m.text());if(m.type()==='error')errors.push(m.text())});
  page.on('pageerror',e=>errors.push(String(e)));
  await page.addInitScript(()=>{
    const data={};window.__testAndroidData=data;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
        if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
        if(key==='loadSchedule')return data.Schedule||'';
        if(key==='loadUiSettings')return data.UiSettings||'{"language":"fr","theme":"light"}';
        if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
        return data[key.replace(/^load/,'')]||'{}';
      };
    }});
  });
  await page.goto(pathToFileURL(asset).href);
  for(const file of fs.readdirSync(chunks).sort()) {
    await page.evaluate(fs.readFileSync(path.join(chunks,file),'utf8')+'\n//# sourceURL='+file);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&window.__edtLazyImportReady);
  await page.evaluate(()=>window.setModeFromAndroid('edit'));
  // Let startup-only legacy callbacks finish, independently of panel interactions.
  await page.waitForTimeout(1200);
  return {page,async close(){
    fs.writeFileSync(path.join(out,name+'.log'),logs.join('\n'));
    await context.close();assert.deepEqual(errors,[],name+': JavaScript errors');
  }};
}
async function interactions() {
  const app=await openApp('interactions'),p=app.page;
  const week=await p.evaluate(()=>currentWeek);
  for(let i=0;i<3;i++) {
    await p.locator('#settingsBtn').tap();
    await p.locator('#settingsX').tap();
    await p.evaluate(()=>new Promise(r=>requestAnimationFrame(r)));
    assert.equal(await p.evaluate(()=>currentWeek),week,'closing Settings must not click the week beneath it');
  }
  // Keyboard-generated clicks and a new rapid gesture must each work once.
  await p.locator('#settingsBtn').focus();await p.keyboard.press('Enter');
  assert.equal(await p.evaluate(()=>__edtHeavyPanels648.isOpen('settings')),true);
  await p.locator('#settingsX').focus();await p.keyboard.press('Enter');
  assert.equal(await p.evaluate(()=>__edtHeavyPanels648.isOpen('settings')),false);
  await p.locator('#editList .editCourse').first().tap();
  await p.locator('#fLabel').tap();
  assert.equal(await p.evaluate(()=>__edtHeavyPanels648.isOpen('course')),true,'touching a field must not close the editor');
  await p.locator('#fLabel').fill('Test cours 6.49');
  await p.locator('#fRoom').fill('Salle test');
  await p.locator('#fCourseBadge').fill('Test badge');
  await p.locator('#fullCourseEnable84').check();
  assert.equal(await p.locator('#fullCourseColor').isEnabled(),true);
  await p.locator('#fullCourseEnable84').uncheck();
  await p.locator('#courseForm button[type=submit]').tap();
  await p.waitForFunction(()=>!__edtHeavyPanels648.isOpen('course'));
  assert.deepEqual(await p.evaluate(()=>Object.keys(JSON.parse(__testAndroidData.Schedule)._weeks)),['A','B','C','D']);
  await p.locator('#editList .editCourse').filter({hasText:'Test cours 6.49'}).tap();
  assert.equal(await p.locator('#fLabel').inputValue(),'Test cours 6.49');
  assert.equal(await p.locator('#fRoom').inputValue(),'Salle test');
  assert.equal(await p.locator('#fCourseBadge').inputValue(),'Test badge');
  await p.screenshot({path:path.join(out,'editor.png')});
  await p.locator('#fLabel').fill('Modification annulée');
  await p.locator('#cancelEdit').tap();
  await p.locator('#editList .editCourse').filter({hasText:'Test cours 6.49'}).tap();
  assert.equal(await p.locator('#fLabel').inputValue(),'Test cours 6.49');
  await p.locator('#cancelEdit').tap();
  // Existing non-standard hours must survive selecting and saving their custom option.
  await p.evaluate(()=>{
    state[selected].courses[0].start='08:10';state[selected].courses[0].end='08:55';state[selected].courses[0].slot=0;
    __edtHeavyPanels648.openCourse(0);
  });
  assert.equal(await p.locator('#fSlot').inputValue(),'0');
  await p.locator('#courseForm button[type=submit]').tap();
  await p.waitForFunction(()=>!__edtHeavyPanels648.isOpen('course'));
  assert.equal(await p.evaluate(()=>state[selected].courses[0].start),'08:10');
  assert.equal(await p.evaluate(()=>state[selected].courses[0].end),'08:55');
  await p.locator('#settingsBtn').tap();
  await p.locator('#languageSelect').tap();await p.keyboard.press('Escape');
  assert.equal(await p.evaluate(()=>__edtHeavyPanels648.isOpen('settings')),true);
  await p.screenshot({path:path.join(out,'settings.png')});
  await p.locator('#settingsX').tap();
  await app.close();console.log('browser_panel_interactions=passed');
}
async function stress(name) {
  const app=await openApp(name),p=app.page;
  const reports=await p.evaluate(name=>__edtHeavyPanelMetrics648.run(name,300),name);
  fs.writeFileSync(path.join(out,name+'.json'),JSON.stringify(reports,null,2));
  for(const r of reports) {
    const count=name==='mixed'?150:300;
    assert.equal(r.openN,count);assert.equal(r.closeN,count);
    for(const key of ['listenerDelta','observerDelta','resizeObserverDelta','nodeDelta','errorDelta','scenarioRenders','directBridgeCalls','directStorageReads','directStorageWrites','directListenerAdds','directObserverDelta','directResizeObserverDelta'])assert.equal(r[key],0,name+'/'+key);
    assert.ok(r.mutationTotal<=16&&r.addedTotal<=4&&r.removedTotal<=4,name+': repeated DOM rebuild');
    // The new architecture must also stop the formerly ambient read/observer loops.
    assert.equal(r.scenarioBridgeCalls,0,name+': ambient Java reads');
    assert.equal(r.scenarioStorageReads,0,name+': ambient storage reads');
    assert.equal(r.scenarioMainMutations,0,name+': background timetable mutations');
  }
  console.log('browser_'+name+'_300_cycles=passed');
  await app.close();return reports;
}
(async()=>{
  try {
    browser=await chromium.launch({headless:true,...(process.env.EDT_BROWSER_CHANNEL?{channel:process.env.EDT_BROWSER_CHANNEL}:{})});
    await interactions();
    const reports={};for(const name of ['settings','course','mixed'])reports[name]=await stress(name);
    fs.writeFileSync(path.join(out,'results.json'),JSON.stringify(reports,null,2));
  } finally {if(browser)await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
