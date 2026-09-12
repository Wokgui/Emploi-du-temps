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
  const browser=await chromium.launch({headless:true,...(process.env.EDT_BROWSER_CHANNEL?{channel:process.env.EDT_BROWSER_CHANNEL}:{})});
  const context=await browser.newContext({viewport:{width:412,height:915},deviceScaleFactor:1,isMobile:true,hasTouch:true});
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
    await page.evaluate(fs.readFileSync(path.join(chunks,file),'utf8')+'\n//# sourceURL='+file);
    await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(resolve)));
  }
  await page.waitForFunction(()=>window.__edtRenderPipeline650&&window.__edtHeavyPanels648);
  await page.evaluate(()=>window.setModeFromAndroid('edit'));
  await page.waitForTimeout(900);

  // Course submit: every historical color/badge/widget wrapper may ask to save, but 6.50
  // must collapse the whole synchronous submit into one native schedule write.
  await page.locator('#editList .editCourse').first().tap();
  await page.locator('#fRoom').fill('Pipeline 650');
  if(await page.locator('#fCourseBadge').count())await page.locator('#fCourseBadge').fill('P650');
  const beforeSubmit=await page.evaluate(()=>({
    native:__testAndroidCalls.saveSchedule||0,
    requests:__edtRenderPipeline650.stats.saveRequests,
    executions:__edtRenderPipeline650.stats.saveExecutions,
    coalesced:__edtRenderPipeline650.stats.coalescedSaves
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
  assert.ok(afterSubmit.requests-beforeSubmit.requests>=1,'course submit must request a save');

  // Day changes: only the course list is dynamic. Static day tabs, break settings and the
  // nine slot rows must keep their exact DOM identity across a long session.
  const dayReport=await page.evaluate(()=>{
    const slotRoot=document.getElementById('slotSettings');
    const firstSlot=slotRoot.firstElementChild;
    const breakRoot=document.querySelector('.breakSettings');
    const firstBreak=breakRoot.firstElementChild;
    const tabs=document.getElementById('dayTabs');
    const firstTab=tabs.querySelector('.dayTab:not(.weekendAdd)');
    const nodesBefore=document.getElementsByTagName('*').length;
    const listenersBefore=window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0;
    const samples=[];
    const days=[2,3,4,5,6];
    for(let i=0;i<300;i++){
      const t=performance.now();
      __edtRenderPipeline650.fastSwitchDay(days[i%days.length]);
      samples.push(performance.now()-t);
    }
    const nodesAfter=document.getElementsByTagName('*').length;
    const listenersAfter=window.__edtHeavyPerfPrelude648?__edtHeavyPerfPrelude648.counters.listenerAdds:0;
    function p50(a){const s=a.slice().sort((x,y)=>x-y);return s[Math.floor(s.length/2)]||0}
    return {
      slotIdentity:firstSlot===slotRoot.firstElementChild,
      breakIdentity:firstBreak===breakRoot.firstElementChild,
      tabIdentity:firstTab===tabs.querySelector('.dayTab:not(.weekendAdd)'),
      slotRows:slotRoot.querySelectorAll(':scope > .slotRow').length,
      nodeDelta:nodesAfter-nodesBefore,listenerDelta:listenersAfter-listenersBefore,
      headP50:p50(samples.slice(0,30)),tailP50:p50(samples.slice(-30)),
      switches:__edtRenderPipeline650.stats.fastDaySwitches
    };
  });
  assert.equal(dayReport.slotIdentity,true,'slot rows must stay mounted while changing day');
  assert.equal(dayReport.breakIdentity,true,'break controls must stay mounted while changing day');
  assert.equal(dayReport.tabIdentity,true,'day tabs must stay mounted while changing day');
  assert.equal(dayReport.slotRows,9,'all nine slot rows must remain available');
  assert.equal(dayReport.listenerDelta,0,'day switching must not add listeners');
  assert.ok(Math.abs(dayReport.nodeDelta)<=4,'day switching must not leak DOM nodes');
  assert.ok(dayReport.tailP50<=Math.max(dayReport.headP50*1.8,dayReport.headP50+2),'day switching must not progressively slow down');

  // Editing a slot must preserve the slot DOM and suppress the old whole-view render.
  const slotReport=await page.evaluate(()=>{
    const root=document.getElementById('slotSettings'),row=root.querySelector('.slotRow'),anchor=row;
    const input=row.querySelector('input[type=time]');
    const beforeNative=__testAndroidCalls.saveSchedule||0;
    const beforeSuppressed=__edtRenderPipeline650.stats.suppressedRenders;
    input.value='08:05';input.dispatchEvent(new Event('change',{bubbles:true}));
    return {
      identity:anchor===root.querySelector('.slotRow'),
      nativeWrites:(__testAndroidCalls.saveSchedule||0)-beforeNative,
      suppressed:__edtRenderPipeline650.stats.suppressedRenders-beforeSuppressed,
      saved:JSON.parse(__testAndroidData.Schedule)._slots[0].start,
      slotEdits:__edtRenderPipeline650.stats.slotEdits
    };
  });
  assert.equal(slotReport.identity,true,'slot editing must not rebuild slot rows');
  assert.equal(slotReport.nativeWrites,1,'slot editing must persist once');
  assert.ok(slotReport.suppressed>=1,'slot editing must suppress the legacy whole-view render');
  assert.equal(slotReport.saved,'08:05');

  fs.writeFileSync(path.join(out,'pipeline-650.json'),JSON.stringify({beforeSubmit,afterSubmit,dayReport,slotReport},null,2));
  fs.writeFileSync(path.join(out,'pipeline-650.log'),logs.join('\n'));
  assert.deepEqual(errors,[],'6.50 pipeline JavaScript errors');
  console.log('browser_render_pipeline_650=passed');
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exitCode=1});
