const {chromium}=require('playwright');
const fs=require('fs');
const path=require('path');
const assert=require('node:assert/strict');
const {pathToFileURL}=require('url');

const root=path.resolve(__dirname,'../..');
const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');

(async()=>{
  const browser=await chromium.launch({headless:true});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage(),errors=[];
  page.on('pageerror',error=>errors.push(String(error)));
  page.on('console',message=>{if(message.type()==='error')errors.push(message.text())});
  await page.addInitScript(()=>{
    const data={AdvancedSettings:'{"cycleLength":2,"singleWeek":false,"showLunchWeek":true,"showBreaksWeek":true}',UiSettings:'{"language":"fr","theme":"blue"}'};
    window.__data763=data;window.alert=()=>{};window.confirm=()=>true;
    window.AndroidSchedule=new Proxy({},{get(target,key){if(typeof key!=='string')return;return(...args)=>{
      if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
      if(key==='loadSchedule')return data.Schedule||'';
      if(key==='loadUiSettings')return data.UiSettings;
      if(key==='loadAdvancedSettings')return data.AdvancedSettings;
      if(key==='listProfiles')return JSON.stringify({current:'main',profiles:[{id:'main',name:'Principal'}]});
      if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
      return data[key.replace(/^load/,'')]||'{}';
    }}});
  });
  await page.goto(pathToFileURL(path.join(root,'app/src/main/assets/index.html')).href);
  for(const name of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,name),'utf8')+'\n//# sourceURL='+name);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&document.getElementById('addBulkCourses'));

  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648.isOpen('settings'));
  const settings=await page.evaluate(()=>{
    const widget=document.getElementById('widgetSettings86'),breaks=document.getElementById('breakSettings86');widget.open=true;breaks.open=true;
    const body=breaks.querySelector(':scope>.settingsSectionBody86');
    return {
      widgetSubtitle:getComputedStyle(document.getElementById('advWidgetTitle')).display,
      breakOrder:[...body.children].map(x=>x.id),
      namesParent:document.querySelector('.breakSettings')?.closest('.settingBox')?.id,
      namesSection:document.querySelector('.breakSettings')?.closest('.settingsSection86')?.id,
      title:document.getElementById('breakNamesTitle763')?.textContent.trim(),
      editHasNames:!!document.querySelector('#viewEdit .breakSettings')
    };
  });
  assert.deepEqual(settings,{widgetSubtitle:'none',breakOrder:['breakDisplaySetting','breakNamesSettings763','week658LunchSettings'],namesParent:'breakNamesSettings763',namesSection:'breakSettings86',title:'Noms des interruptions',editHasNames:false});
  await page.locator('#settingsDone').tap();
  await page.locator('.nav[data-mode="edit"]').tap();
  await page.waitForFunction(()=>document.getElementById('viewEdit').classList.contains('active'));

  const controls=await page.evaluate(()=>{
    const info=id=>{const el=document.getElementById(id),r=el.getBoundingClientRect();return{id,width:r.width,center:(r.left+r.right)/2,text:el.textContent.trim()}};
    const importButton=info('importPhoto'),add=info('addCourse'),bulk=info('addBulkCourses'),history=document.getElementById('editHistoryActions86');
    return {importButton,add,bulk,viewport:innerWidth,historyAfterBulk:history.previousElementSibling?.id==='addBulkCourses',historyButtons:[...history.children].map(x=>x.id)};
  });
  for(const item of [controls.importButton,controls.add,controls.bulk]){
    assert.ok(item.width<controls.viewport-20,JSON.stringify(controls));
    assert.ok(Math.abs(item.center-controls.viewport/2)<2,JSON.stringify(controls));
  }
  assert.equal(controls.historyAfterBulk,true);
  assert.deepEqual(controls.historyButtons,['undoEdit86','redoEdit86']);

  const navigation=await page.evaluate(async()=>{
    const times=[],nodesBefore=document.getElementsByTagName('*').length,cache=window.__edtNavigationCacheV2.cache;
    let immediateFailures=0;
    for(let i=0;i<300;i++){
      document.querySelector('.nav[data-mode="today"]').click();
      if(i%20===0)window.invalidateTimetableViews(['edit']);
      const start=performance.now();document.querySelector('.nav[data-mode="edit"]').click();times.push(performance.now()-start);
      if(!document.getElementById('viewEdit').classList.contains('active'))immediateFailures++;
      if(i%25===0)await new Promise(resolve=>requestAnimationFrame(resolve));
    }
    times.sort((a,b)=>a-b);
    return {immediateFailures,p50:times[149],p95:times[284],max:times[299],nodesDelta:document.getElementsByTagName('*').length-nodesBefore,navs:cache.navs,hits:cache.hits};
  });
  assert.equal(navigation.immediateFailures,0,JSON.stringify(navigation));
  assert.ok(navigation.p95<50,JSON.stringify(navigation));
  assert.ok(navigation.nodesDelta<=2,JSON.stringify(navigation));
  assert.ok(navigation.hits>=500,JSON.stringify(navigation));
  assert.deepEqual(errors,[]);
  console.log(JSON.stringify({settings,controls,navigation,errors},null,2));
  await browser.close();
})().catch(error=>{console.error(error);process.exit(1)});
