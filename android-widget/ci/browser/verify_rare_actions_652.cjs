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
    const median=a=>{const s=a.slice().sort((x,y)=>x-y);return s[Math.floor(s.length/2)]||0};
    const runBlock=async(cycles)=>{
      const before=snap(),samples=[],listenerEvents=[];
      let previousListeners=before.listeners;
      for(let i=0;i<cycles;i++){
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
      if(perf.listenerAdds!==previousListeners)listenerEvents.push({cycle:cycles,id:null,delta:perf.listenerAdds-previousListeners,total:perf.listenerAdds,phase:'finalFrames'});
      const after=snap();
      return {before,after,listenerEvents,headP50:median(samples.slice(0,20)),tailP50:median(samples.slice(-20))};
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
    const block1=await runBlock(120);
    const block2=await runBlock(120);
    return {controls:controls.map(x=>x.id),cold,warmup,block1,block2,stats:chain.stats};
  });

  fs.writeFileSync(path.join(out,'rare-actions-652.json'),JSON.stringify(report,null,2));
  fs.writeFileSync(path.join(out,'rare-actions-652.log'),logs.join('\n'));
  console.log('browser_rare_actions_652_report',JSON.stringify(report));
  console.log('rare_actions_652_listener_events',JSON.stringify({warmup:report.warmup,block1:report.block1.listenerEvents,block2:report.block2.listenerEvents}));
  assert.ok(report.controls.length>=3,'rare-action suite must resolve school/holiday/profile controls');
  assert.equal(report.block2.after.listeners-report.block2.before.listeners,0,'rare actions must not progressively accumulate listeners after a complete exercised block');
  assert.equal(report.block2.after.observers-report.block2.before.observers,0,'rare actions must not progressively accumulate MutationObservers');
  assert.equal(report.block2.after.resize-report.block2.before.resize,0,'rare actions must not progressively accumulate ResizeObservers');
  assert.ok(report.block2.after.nodes-report.block2.before.nodes<=8,'rare actions must not progressively grow the DOM');
  assert.ok(report.block2.tailP50<=Math.max(report.block2.headP50*2,report.block2.headP50+5),'rare actions must not progressively slow down');
  assert.deepEqual(errors,[],'6.52 rare-action JavaScript errors');
  console.log('browser_rare_actions_652=passed',JSON.stringify(report));
  await context.close();await browser.close();
})().catch(e=>{console.error(e);process.exit(1)});
