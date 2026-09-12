const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const {pathToFileURL}=require('node:url');

const chunks=path.resolve(process.env.EDT_UI_CHUNKS||'smoke-browser/chunks');
const out=path.resolve(process.env.EDT_BROWSER_OUTPUT||'smoke-browser');
const asset=path.resolve(__dirname,'../../app/src/main/assets/index.html');
fs.mkdirSync(out,{recursive:true});

(async()=>{
  const browser=await chromium.launch({headless:true});
  const context=await browser.newContext({viewport:{width:412,height:915},isMobile:true,hasTouch:true});
  const page=await context.newPage();
  const errors=[],logs=[];
  page.on('pageerror',e=>errors.push(String(e)));
  page.on('console',m=>{logs.push(m.text());if(m.type()==='error')errors.push(m.text())});
  await page.addInitScript(()=>{
    const data={},calls={};window.__testAndroidData=data;window.__testAndroidCalls=calls;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
        calls[key]=(calls[key]||0)+1;
        if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
        if(key==='loadSchedule')return data.Schedule||'';
        if(key==='loadUiSettings')return data.UiSettings||'{"language":"fr","theme":"light"}';
        if(key.startsWith('list')||key.startsWith('supported')||key==='loadLanguagePacks'||key==='loadEffectiveCourses')return '[]';
        return data[key.replace(/^load/,'')]||'{}';
      };
    }});
  });
  await page.goto(pathToFileURL(asset).href);
  for(const file of fs.readdirSync(chunks).sort()){
    await page.evaluate(fs.readFileSync(path.join(chunks,file),'utf8'));
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtRenderPipeline650&&window.__edtHeavyPanels648);
  await page.evaluate(()=>window.setModeFromAndroid('edit'));
  await page.waitForTimeout(600);

  await page.locator('#editList .editCourse').first().tap();
  await page.locator('#fRoom').fill('Pipeline 650');
  if(await page.locator('#fCourseBadge').count())await page.locator('#fCourseBadge').fill('P650');
  const beforeSubmit=await page.evaluate(()=>({
    native:__testAndroidCalls.saveSchedule||0,
    requests:__edtRenderPipeline650.stats.saveRequests,
    executions:__edtRenderPipeline650.stats.saveExecutions
  }));
  await page.locator('#courseForm button[type=submit]').tap();
  await page.waitForFunction(()=>!__edtHeavyPanels648.isOpen('course'));
  const afterSubmit=await page.evaluate(()=>({
    native:__testAndroidCalls.saveSchedule||0,
    requests:__edtRenderPipeline650.stats.saveRequests,
    executions:__edtRenderPipeline650.stats.saveExecutions,
    coalesced:__edtRenderPipeline650.stats.coalescedSaves
  }));
  assert.equal(afterSubmit.native-beforeSubmit.native,1,'one course submit must write the schedule once');
  assert.equal(afterSubmit.executions-beforeSubmit.executions,1,'one course submit must execute one save');

  const dayReport=await page.evaluate(()=>{
    const startDay=Number(selected);
    const slotRoot=document.getElementById('slotSettings'),firstSlot=slotRoot.firstElementChild;
    const breakRoot=document.querySelector('.breakSettings'),firstBreak=breakRoot.firstElementChild;
    const tabs=document.getElementById('dayTabs'),firstTab=tabs.querySelector('.dayTab:not(.weekendAdd)');
    const nodesBefore=document.getElementsByTagName('*').length;
    const listenersBefore=window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0;
    const samples=[],days=[2,3,4,5,6];
    for(let i=0;i<300;i++){
      const t=performance.now();__edtRenderPipeline650.fastSwitchDay(days[i%days.length]);samples.push(performance.now()-t);
    }
    // Compare identical UI state. Different weekdays legitimately contain different numbers of course nodes.
    __edtRenderPipeline650.fastSwitchDay(startDay);
    const nodesAfter=document.getElementsByTagName('*').length;
    const listenersAfter=window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0;
    const p50=a=>{const s=a.slice().sort((x,y)=>x-y);return s[Math.floor(s.length/2)]||0};
    return {
      startDay,finalDay:Number(selected),slotIdentity:firstSlot===slotRoot.firstElementChild,
      breakIdentity:firstBreak===breakRoot.firstElementChild,tabIdentity:firstTab===tabs.querySelector('.dayTab:not(.weekendAdd)'),
      slotRows:slotRoot.querySelectorAll(':scope > .slotRow').length,nodeDelta:nodesAfter-nodesBefore,
      listenerDelta:listenersAfter-listenersBefore,headP50:p50(samples.slice(0,30)),tailP50:p50(samples.slice(-30))
    };
  });
  assert.equal(dayReport.finalDay,dayReport.startDay,'day stress must finish in the same UI state');
  assert.equal(dayReport.slotIdentity,true,'slot rows must stay mounted');
  assert.equal(dayReport.breakIdentity,true,'break controls must stay mounted');
  assert.equal(dayReport.tabIdentity,true,'day tabs must stay mounted');
  assert.equal(dayReport.slotRows,9);
  assert.equal(dayReport.listenerDelta,0,'day switching must not add listeners');
  assert.ok(dayReport.nodeDelta<=0,'same-state day switching must not accumulate DOM nodes');
  assert.ok(dayReport.tailP50<=Math.max(dayReport.headP50*1.8,dayReport.headP50+2),'day switching must not progressively slow down');

  const slotReport=await page.evaluate(()=>{
    const root=document.getElementById('slotSettings'),row=root.querySelector('.slotRow'),anchor=row,input=row.querySelector('input[type=time]');
    const beforeNative=__testAndroidCalls.saveSchedule||0,beforeSuppressed=__edtRenderPipeline650.stats.suppressedRenders;
    input.value='08:05';input.dispatchEvent(new Event('change',{bubbles:true}));
    return {identity:anchor===root.querySelector('.slotRow'),nativeWrites:(__testAndroidCalls.saveSchedule||0)-beforeNative,
      suppressed:__edtRenderPipeline650.stats.suppressedRenders-beforeSuppressed,saved:JSON.parse(__testAndroidData.Schedule)._slots[0].start};
  });
  assert.equal(slotReport.identity,true,'slot editing must not rebuild slot rows');
  assert.equal(slotReport.nativeWrites,1,'slot editing must persist once');
  assert.ok(slotReport.suppressed>=1,'slot editing must suppress the legacy whole-view render');
  assert.equal(slotReport.saved,'08:05');

  const report={beforeSubmit,afterSubmit,dayReport,slotReport};
  fs.writeFileSync(path.join(out,'pipeline-650.json'),JSON.stringify(report,null,2));
  fs.writeFileSync(path.join(out,'pipeline-650.log'),logs.join('\n'));
  assert.deepEqual(errors,[],'6.50 pipeline JavaScript errors');
  console.log('browser_render_pipeline_650=passed',JSON.stringify(report));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
