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
  page.on('dialog',d=>d.accept());
  await page.addInitScript(()=>{
    const data={},calls={};window.__testAndroidData=data;window.__testAndroidCalls=calls;
    window.AndroidSchedule=new Proxy({}, {get(target,key){
      if(typeof key!=='string')return;
      return (...args)=>{
        calls[key]=(calls[key]||0)+1;
        if(key.startsWith('save')){data[key.slice(4)]=args[0];return true}
        if(key==='loadSchedule')return data.Schedule||'';
        if(key==='loadUiSettings')return data.UiSettings||'{"language":"fr","theme":"blue"}';
        if(key==='loadAdvancedSettings')return data.AdvancedSettings||'{"cycleLength":2,"singleWeek":false,"schoolEnabled":false,"schoolYear":"2026-2027","schoolZone":"B"}';
        if(key==='listProfiles')return '{"profiles":[{"id":"default","name":"Défaut"},{"id":"p2","name":"Test"}],"current":"default"}';
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
  await page.waitForFunction(()=>window.__edtActionChains651&&window.__edtHeavyPerfPrelude648);
  await page.locator('#settingsBtn').tap();
  await page.waitForFunction(()=>window.__edtHeavyPanels648&&__edtHeavyPanels648.isOpen('settings'));

  const report=await page.evaluate(async()=>{
    const chain=window.__edtActionChains651,perf=window.__edtHeavyPerfPrelude648.counters;
    const controls=['schoolEnabled','schoolYear','schoolZone','advHoliday','advProfileSelect'].map(id=>document.getElementById(id)).filter(Boolean);
    const snap=()=>({listeners:perf.listenerAdds,observers:perf.mutationObservers,resize:perf.resizeObservers,nodes:document.getElementsByTagName('*').length,
      actions:chain.stats.actions,targeted:chain.stats.targetedRenders,suppressed:chain.stats.suppressedRenders,calls:Object.assign({},window.__testAndroidCalls)});
    const exercise=el=>{
      if(el.tagName==='SELECT'&&el.options.length>1)el.selectedIndex=(el.selectedIndex+1)%el.options.length;
      else if(el.type==='checkbox')el.checked=!el.checked;
      el.dispatchEvent(new Event('change',{bubbles:true}));
    };
    const cold=snap();
    const warmup=[];
    for(const el of controls){
      const before=snap();
      exercise(el);
      await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
      const after=snap();
      warmup.push({id:el.id,listenerDelta:after.listeners-before.listeners,observerDelta:after.observers-before.observers,resizeDelta:after.resize-before.resize,nodeDelta:after.nodes-before.nodes});
    }
    const before=snap();
    const samples=[];
    const listenerEvents=[];
    let previousListeners=before.listeners;
    for(let i=0;i<120;i++){
      const t=performance.now();
      for(const el of controls){
        const listenerBefore=perf.listenerAdds;
        exercise(el);
        const listenerAfter=perf.listenerAdds;
        if(listenerAfter!==listenerBefore)listenerEvents.push({cycle:i,id:el.id,delta:listenerAfter-listenerBefore,total:listenerAfter,phase:'dispatch'});
      }
      samples.push(performance.now()-t);
      if(i%20===0){
        await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
        if(perf.listenerAdds!==previousListeners)listenerEvents.push({cycle:i,id:null,delta:perf.listenerAdds-previousListeners,total:perf.listenerAdds,phase:'afterFrames'});
        previousListeners=perf.listenerAdds;
      }
    }
    await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));
    if(perf.listenerAdds!==previousListeners)listenerEvents.push({cycle:120,id:null,delta:perf.listenerAdds-previousListeners,total:perf.listenerAdds,phase:'finalFrames'});
    const after=snap();
    const median=a=>{const s=a.slice().sort((x,y)=>x-y);return s[Math.floor(s.length/2)]||0};
    return {controls:controls.map(x=>x.id),cold,warmup,before,after,listenerEvents,headP50:median(samples.slice(0,20)),tailP50:median(samples.slice(-20)),stats:chain.stats};
  });

  fs.writeFileSync(path.join(out,'rare-actions-652.json'),JSON.stringify(report,null,2));
  fs.writeFileSync(path.join(out,'rare-actions-652.log'),logs.join('\n'));
  console.log('browser_rare_actions_652_report',JSON.stringify(report));
  assert.ok(report.controls.length>=3,'rare-action suite must resolve school/holiday/profile controls');
  assert.equal(report.after.listeners-report.before.listeners,0,'rare actions must not accumulate listeners after first-use initialization');
  assert.equal(report.after.observers-report.before.observers,0,'rare actions must not accumulate MutationObservers after first-use initialization');
  assert.equal(report.after.resize-report.before.resize,0,'rare actions must not accumulate ResizeObservers after first-use initialization');
  assert.ok(report.after.nodes-report.before.nodes<=8,'rare actions must not progressively grow the DOM');
  assert.ok(report.tailP50<=Math.max(report.headP50*2,report.headP50+5),'rare actions must not progressively slow down');
  assert.deepEqual(errors,[],'6.52 rare-action JavaScript errors');
  console.log('browser_rare_actions_652=passed',JSON.stringify(report));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
